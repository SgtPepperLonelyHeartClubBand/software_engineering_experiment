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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QaExceptionScenarioTests {

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
    void itemAndMessageValidationRejectInvalidRequests() throws Exception {
        Long sellerId = ensureUser("220001001", "QA参数卖家");
        Long buyerId = ensureUser("220001002", "QA参数买家");

        mockMvc.perform(post("/api/items")
                        .header(DEV_USER_HEADER, sellerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "缺少图片的商品",
                                  "category": "专业书籍",
                                  "condition": "9成新",
                                  "price": 20.00,
                                  "locationCode": "JLH-MY-01",
                                  "description": "非法发布测试",
                                  "imageUrls": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(post("/api/items")
                        .header(DEV_USER_HEADER, sellerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validItemJson("价格非法商品", "-1.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(post("/api/items")
                        .header(DEV_USER_HEADER, sellerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validItemJson("超长标题".repeat(11), "20.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        Long itemId = createItem(sellerId, "QA消息校验商品");
        Long conversationId = ensureConversation(itemId, buyerId);

        mockMvc.perform(post("/api/conversations/{id}/messages", conversationId)
                        .header(DEV_USER_HEADER, buyerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", " "))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(post("/api/conversations/{id}/messages", conversationId)
                        .header(DEV_USER_HEADER, buyerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "a".repeat(201)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void abnormalTradeStateAndPermissionGuardsWork() throws Exception {
        Long sellerId = ensureUser("220001011", "QA交易卖家");
        Long buyerId = ensureUser("220001012", "QA交易买家");
        Long strangerId = ensureUser("220001013", "QA交易路人");
        Long itemId = createItem(sellerId, "QA异常交易商品");

        mockMvc.perform(post("/api/items/{id}/reserve", itemId).header(DEV_USER_HEADER, sellerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        JsonNode order = reserve(itemId, buyerId);
        Long orderId = order.path("id").asLong();

        mockMvc.perform(post("/api/orders/{id}/cancel", orderId).header(DEV_USER_HEADER, strangerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(post("/api/orders/{id}/complete", orderId).header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(post("/api/orders/{id}/complete", orderId).header(DEV_USER_HEADER, sellerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.itemStatus").value("已完成"));

        mockMvc.perform(post("/api/orders/{id}/cancel", orderId).header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(post("/api/items/{id}/reserve", itemId).header(DEV_USER_HEADER, strangerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void deletedItemAndSelfInteractionGuardsWork() throws Exception {
        Long sellerId = ensureUser("220001021", "QA删除卖家");
        Long buyerId = ensureUser("220001022", "QA删除买家");
        Long deletedItemId = createItem(sellerId, "QA已下架商品");

        mockMvc.perform(delete("/api/items/{id}", deletedItemId).header(DEV_USER_HEADER, sellerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/items/{id}/reserve", deletedItemId).header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        mockMvc.perform(post("/api/items/{id}/favorite", deletedItemId).header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        mockMvc.perform(post("/api/conversations")
                        .header(DEV_USER_HEADER, buyerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "itemId": %d }
                                """.formatted(deletedItemId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        Long ownItemId = createItem(sellerId, "QA自操作商品");

        mockMvc.perform(post("/api/items/{id}/favorite", ownItemId).header(DEV_USER_HEADER, sellerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(post("/api/conversations")
                        .header(DEV_USER_HEADER, sellerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "itemId": %d }
                                """.formatted(ownItemId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void conversationSecurityAndQuoteGuardsWork() throws Exception {
        Long sellerId = ensureUser("220001031", "QA私信卖家");
        Long buyerId = ensureUser("220001032", "QA私信买家");
        Long strangerId = ensureUser("220001033", "QA私信路人");
        Long anotherBuyerId = ensureUser("220001034", "QA另一个买家");

        Long itemId = createItem(sellerId, "QA私信商品A");
        Long conversationId = ensureConversation(itemId, buyerId);
        Long buyerMessageId = sendMessage(conversationId, buyerId, "这本书今晚可以取吗", null);

        mockMvc.perform(get("/api/conversations/{id}/messages", conversationId).header(DEV_USER_HEADER, strangerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(post("/api/conversations/{id}/messages", conversationId)
                        .header(DEV_USER_HEADER, strangerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "我不是会话参与者"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        Long secondItemId = createItem(sellerId, "QA私信商品B");
        Long secondConversationId = ensureConversation(secondItemId, anotherBuyerId);
        Long otherConversationMessageId = sendMessage(secondConversationId, anotherBuyerId, "另一个会话的消息", null);

        mockMvc.perform(post("/api/conversations/{id}/messages", conversationId)
                        .header(DEV_USER_HEADER, buyerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "content", "尝试跨会话引用",
                                "quoteMessageId", otherConversationMessageId
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(post("/api/messages/{id}/recall", buyerMessageId).header(DEV_USER_HEADER, sellerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(post("/api/messages/{id}/recall", buyerMessageId).header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.recalled").value(true));

        mockMvc.perform(post("/api/conversations/{id}/messages", conversationId)
                        .header(DEV_USER_HEADER, sellerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "content", "不能引用已撤回消息",
                                "quoteMessageId", buyerMessageId
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
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
                        .content(validItemJson(title, "35.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    private String validItemJson(String title, String price) {
        return """
                {
                  "title": "%s",
                  "category": "专业书籍",
                  "condition": "9成新",
                  "price": %s,
                  "locationCode": "JLH-MY-01",
                  "description": "QA异常场景测试商品",
                  "imageUrls": ["/uploads/qa-exception.png"]
                }
                """.formatted(title, price);
    }

    private JsonNode reserve(Long itemId, Long buyerId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/items/{id}/reserve", itemId).header(DEV_USER_HEADER, buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("RESERVED"))
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
        String requestBody = quoteMessageId == null
                ? objectMapper.writeValueAsString(Map.of("content", content))
                : objectMapper.writeValueAsString(Map.of("content", content, "quoteMessageId", quoteMessageId));
        MvcResult result = mockMvc.perform(post("/api/conversations/{id}/messages", conversationId)
                        .header(DEV_USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }
}
