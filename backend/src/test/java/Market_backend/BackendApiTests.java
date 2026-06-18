package Market_backend;

import Market_backend.config.JwtUtil;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackendApiTests {

    private static final String DEV_USER_HEADER = "X-Dev-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Test
    void protectedApiRequiresAuthenticationAndAcceptsDevHeaderAndJwt() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/api/users/me").header(DEV_USER_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.studentId").value("220000001"));

        String token = jwtUtil.generateToken(1L, "220000001");
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void locationsAndUserProfileCanBeUpdated() throws Exception {
        mockMvc.perform(get("/api/locations/tree").header(DEV_USER_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].text").exists())
                .andExpect(jsonPath("$.data[0].children").isArray())
                .andExpect(jsonPath("$.data[0].children[0].children[0].children").doesNotExist());

        mockMvc.perform(put("/api/users/me")
                        .header(DEV_USER_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "后端A",
                                  "wechat": "backend_a",
                                  "locationCode": "JLH-MY-02"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.nickname").value("后端A"))
                .andExpect(jsonPath("$.data.avatarUrl").value("https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg"))
                .andExpect(jsonPath("$.data.locationCode").value("JLH-MY-02"))
                .andExpect(jsonPath("$.data.isProfileComplete").value(true));
    }

    @Test
    void itemCrudRespectsFiltersOwnershipAndSoftDelete() throws Exception {
        Long itemId = createItem("后端A专属数据库教材", "专业书籍", "/uploads/db-book.png");

        mockMvc.perform(get("/api/items")
                        .header(DEV_USER_HEADER, "1")
                        .param("category", "专业书籍")
                        .param("keyword", "数据库"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value(itemId))
                .andExpect(jsonPath("$.data[0].status").value("在售"));

        mockMvc.perform(get("/api/items/{id}", itemId).header(DEV_USER_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.images[0]").value("/uploads/db-book.png"))
                .andExpect(jsonPath("$.data.viewCount").value(1));

        Long otherUserId = ensureOtherUser();
        mockMvc.perform(put("/api/items/{id}", itemId)
                        .header(DEV_USER_HEADER, otherUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validItemJson("越权修改", "专业书籍", "/uploads/other.png")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(delete("/api/items/{id}", itemId).header(DEV_USER_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/items/{id}", itemId).header(DEV_USER_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        mockMvc.perform(get("/api/items")
                        .header(DEV_USER_HEADER, "1")
                        .param("keyword", "后端A专属数据库教材"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void imageUploadAcceptsImagesAndRejectsNonImages() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "file",
                "cover.png",
                "image/png",
                new byte[] {1, 2, 3}
        );

        mockMvc.perform(multipart("/api/upload/image")
                        .file(image)
                        .header(DEV_USER_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.url", startsWith("/uploads/")));

        MockMultipartFile text = new MockMultipartFile(
                "file",
                "note.txt",
                "text/plain",
                "hello".getBytes()
        );

        mockMvc.perform(multipart("/api/upload/image")
                        .file(text)
                        .header(DEV_USER_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    private Long createItem(String title, String category, String imageUrl) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/items")
                        .header(DEV_USER_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validItemJson(title, category, imageUrl)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private String validItemJson(String title, String category, String imageUrl) {
        return """
                {
                  "title": "%s",
                  "category": "%s",
                  "condition": "9成新",
                  "price": 25.00,
                  "locationCode": "JLH-MY-01",
                  "description": "测试发布商品",
                  "imageUrls": ["%s"]
                }
                """.formatted(title, category, imageUrl);
    }

    private Long ensureOtherUser() {
        return userRepository.findByStudentId("220000099")
                .map(User::getId)
                .orElseGet(() -> {
                    Location location = locationRepository.findByCode("JLH-MY-01").orElseThrow();
                    User user = new User();
                    user.setStudentId("220000099");
                    user.setEmail("220000099@seu.edu.cn");
                    user.setNickname("另一个用户");
                    user.setLocation(location);
                    user.setIsProfileComplete(true);
                    return userRepository.save(user).getId();
                });
    }
}
