package Market_backend;

import Market_backend.entity.Item;
import Market_backend.entity.Location;
import Market_backend.entity.TradeOrder;
import Market_backend.entity.User;
import Market_backend.repository.ItemRepository;
import Market_backend.repository.LocationRepository;
import Market_backend.repository.TradeOrderRepository;
import Market_backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QaConcurrencyTests {

    private static final String DEV_USER_HEADER = "X-Dev-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TradeOrderRepository tradeOrderRepository;

    @Test
    void concurrentReservationAllowsOnlyOneSuccessfulBuyer() throws Exception {
        Long sellerId = ensureUser("220001101", "QA并发卖家");
        List<Long> buyerIds = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            buyerIds.add(ensureUser("22000111" + i, "QA并发买家" + i));
        }
        Long itemId = createItem(sellerId, "QA并发抢购商品");

        ExecutorService executor = Executors.newFixedThreadPool(buyerIds.size());
        CountDownLatch ready = new CountDownLatch(buyerIds.size());
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<>();

        try {
            for (Long buyerId : buyerIds) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    assertTrue(start.await(5, TimeUnit.SECONDS), "并发测试启动超时");
                    MvcResult result = mockMvc.perform(post("/api/items/{id}/reserve", itemId)
                                    .header(DEV_USER_HEADER, buyerId))
                            .andExpect(status().isOk())
                            .andReturn();
                    return objectMapper.readTree(result.getResponse().getContentAsString())
                            .path("code").asInt();
                }));
            }

            assertTrue(ready.await(5, TimeUnit.SECONDS), "并发请求准备超时");
            start.countDown();

            List<Integer> resultCodes = new ArrayList<>();
            for (Future<Integer> future : futures) {
                resultCodes.add(future.get(10, TimeUnit.SECONDS));
            }

            long successCount = resultCodes.stream().filter(code -> code == 0).count();
            long rejectedCount = resultCodes.stream().filter(code -> code == 400).count();
            assertEquals(1, successCount, "同一商品只能有一个买家预定成功");
            assertEquals(buyerIds.size() - 1, rejectedCount, "其余并发买家应收到商品不可预定错误");

            Item item = itemRepository.findById(itemId).orElseThrow();
            assertEquals(Item.STATUS_RESERVED, item.getStatus());
            assertEquals(1, item.getWantCount());
            assertTrue(tradeOrderRepository.findByItemIdAndStatus(itemId, TradeOrder.STATUS_RESERVED).isPresent());
        } finally {
            executor.shutdownNow();
        }
    }

    private Long ensureUser(String studentId, String nickname) {
        return userRepository.findByStudentId(studentId)
                .map(User::getId)
                .orElseGet(() -> {
                    Location location = locationRepository.findByCode("JLH-MY-01").orElseThrow();
                    User user = new User();
                    user.setStudentId(studentId);
                    user.setEmail(studentId + "@seu.edu.cn");
                    user.setNickname(nickname);
                    user.setLocation(location);
                    user.setIsProfileComplete(true);
                    return userRepository.save(user).getId();
                });
    }

    private Long createItem(Long sellerId, String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/items")
                        .header(DEV_USER_HEADER, sellerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "category": "电子数码",
                                  "condition": "9成新",
                                  "price": 99.00,
                                  "locationCode": "JLH-MY-01",
                                  "description": "QA并发预定测试商品",
                                  "imageUrls": ["/uploads/qa-concurrency.png"]
                                }
                                """.formatted(title)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }
}
