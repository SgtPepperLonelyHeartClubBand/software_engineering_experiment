package Market_backend;

import Market_backend.config.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.security.dev-auth-enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DevAuthDisabledTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void devHeaderIsIgnoredWhenDevAuthIsDisabledButJwtStillWorks() throws Exception {
        mockMvc.perform(get("/api/users/me").header("X-Dev-User-Id", "1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        String token = jwtUtil.generateToken(1L, "220000001");
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token)
                        .header("X-Dev-User-Id", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1));
    }
}
