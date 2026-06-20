package Market_backend;

import Market_backend.entity.Location;
import Market_backend.entity.User;
import Market_backend.repository.LocationRepository;
import Market_backend.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackendBApiTests {

    private static final String DEV_USER_HEADER = "X-Dev-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Test
    void reserveFavoriteAndCompleteFlowWorks() throws Exception {
        Long sellerId = ensureUser("220000201", "卖家同学");
        Long buyerId = ensureUser("220000202", "买家同学");
        Long anotherBuyerId = ensureUser("220000203", "另一个买家");
        Long itemId = createItem(sellerId, "后端B交易测试教材");

        mockMvc.perform(post("/api/items/{id}/favorite", itemId).header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/items/{id}", itemId).header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited").value(true))
                .andExpect(jsonPath("$.data.reservedByMe").value(false));

        mockMvc.perform(get("/api/favorites").header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(itemId));

        JsonNode order = reserve(itemId, buyerId);
        Long orderId = order.path("id").asLong();
        Long conversationId = order.path("conversationId").asLong();

        mockMvc.perform(get("/api/items/{id}", itemId).header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("被预定"))
                .andExpect(jsonPath("$.data.reservedByMe").value(true))
                .andExpect(jsonPath("$.data.activeOrderId").value(orderId));

        mockMvc.perform(post("/api/items/{id}/reserve", itemId).header(DEV_USER_HEADER, anotherBuyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(get("/api/orders/reserved").header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(itemId))
                .andExpect(jsonPath("$.data[0].status").value("被预定"));

        mockMvc.perform(post("/api/orders/{id}/complete", orderId).header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(post("/api/orders/{id}/complete", orderId).header(DEV_USER_HEADER, sellerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.itemStatus").value("已完成"))
                .andExpect(jsonPath("$.data.conversationId").value(conversationId));

        mockMvc.perform(get("/api/orders/bought").header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(itemId))
                .andExpect(jsonPath("$.data[0].status").value("已完成"));

        mockMvc.perform(get("/api/notifications").header(DEV_USER_HEADER, sellerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("RESERVE_CREATED"));

        mockMvc.perform(get("/api/notifications").header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("ORDER_COMPLETED"));
    }

    @Test
    void cancelReservationRestoresOnSaleItem() throws Exception {
        Long sellerId = ensureUser("220000211", "取消卖家");
        Long buyerId = ensureUser("220000212", "取消买家");
        Long itemId = createItem(sellerId, "后端B取消预定测试");
        JsonNode order = reserve(itemId, buyerId);

        mockMvc.perform(post("/api/orders/{id}/cancel", order.path("id").asLong()).header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.itemStatus").value("在售"));

        mockMvc.perform(get("/api/items/{id}", itemId).header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("在售"))
                .andExpect(jsonPath("$.data.reservedByMe").value(false));

        mockMvc.perform(get("/api/orders/reserved").header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void conversationsMessagesReadAndRecallWork() throws Exception {
        Long sellerId = ensureUser("220000221", "消息卖家");
        Long buyerId = ensureUser("220000222", "消息买家");
        Long itemId = createItem(sellerId, "后端B私信测试");

        Long conversationId = ensureConversation(itemId, buyerId);
        Long buyerMessageId = sendMessage(conversationId, buyerId, "你好，这本书还在吗", null);

        mockMvc.perform(get("/api/conversations").header(DEV_USER_HEADER, sellerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(conversationId))
                .andExpect(jsonPath("$.data[0].unread").value(1));

        mockMvc.perform(post("/api/conversations/{id}/read", conversationId).header(DEV_USER_HEADER, sellerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/messages/unread-summary").header(DEV_USER_HEADER, sellerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.chatUnread").value(0));

        Long sellerMessageId = sendMessage(conversationId, sellerId, "还在，可以今晚面交", buyerMessageId);

        mockMvc.perform(get("/api/conversations/{id}/messages", conversationId).header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[1].quote.id").value(buyerMessageId));

        mockMvc.perform(post("/api/messages/{id}/recall", sellerMessageId).header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(post("/api/messages/{id}/recall", sellerMessageId).header(DEV_USER_HEADER, sellerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.recalled").value(true));

        mockMvc.perform(delete("/api/conversations/{id}", conversationId).header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void notificationsCanBeMarkedRead() throws Exception {
        Long sellerId = ensureUser("220000231", "通知卖家");
        Long buyerId = ensureUser("220000232", "通知买家");
        Long itemId = createItem(sellerId, "后端B通知测试");
        reserve(itemId, buyerId);

        MvcResult result = mockMvc.perform(get("/api/notifications").header(DEV_USER_HEADER, sellerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].unread").value(true))
                .andReturn();
        Long notificationId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path(0).path("id").asLong();

        mockMvc.perform(post("/api/notifications/{id}/read", notificationId).header(DEV_USER_HEADER, sellerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/messages/unread-summary").header(DEV_USER_HEADER, sellerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notificationUnread").value(0));
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
                                  "category": "专业书籍",
                                  "condition": "9成新",
                                  "price": 30.00,
                                  "locationCode": "JLH-MY-01",
                                  "description": "后端B测试商品",
                                  "imageUrls": ["/uploads/backend-b.png"]
                                }
                                """.formatted(title)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    private JsonNode reserve(Long itemId, Long buyerId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/items/{id}/reserve", itemId).header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("RESERVED"))
                .andExpect(jsonPath("$.data.itemStatus").value("被预定"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private Long ensureConversation(Long itemId, Long userId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/conversations")
                        .header(DEV_USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "itemId": %d }
                                """.formatted(itemId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    private Long sendMessage(Long conversationId, Long userId, String content, Long quoteMessageId) throws Exception {
        String quote = quoteMessageId == null ? "null" : quoteMessageId.toString();
        MvcResult result = mockMvc.perform(post("/api/conversations/{id}/messages", conversationId)
                        .header(DEV_USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "%s",
                                  "quoteMessageId": %s
                                }
                                """.formatted(content, quote)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }
}
