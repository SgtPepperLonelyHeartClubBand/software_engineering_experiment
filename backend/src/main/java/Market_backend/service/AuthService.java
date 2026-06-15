package Market_backend.service;

import Market_backend.common.BusinessException;
import Market_backend.dto.LoginResponseVO;
import Market_backend.entity.User;
import Market_backend.repository.UserRepository;
import Market_backend.config.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final VerificationCodeStore verificationCodeStore;
    private final JwtUtil jwtUtil;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${auth.dev-fixed-code:123456}")
    private String devFixedCode;

    @Value("${auth.code-expiration-minutes:5}")
    private long codeExpirationMinutes;

    @Value("${auth.send-interval-seconds:60}")
    private long sendIntervalSeconds;

    public AuthService(
            UserRepository userRepository,
            VerificationCodeStore verificationCodeStore,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.verificationCodeStore = verificationCodeStore;
        this.jwtUtil = jwtUtil;
    }

    public void sendCode(String studentId) {
        String email = buildEmail(studentId);

        if (!verificationCodeStore.canSend(email, sendIntervalSeconds)) {
            throw new BusinessException(429, "发送过于频繁，请稍后再试");
        }

        String code = generateCode();
        verificationCodeStore.save(email, code, codeExpirationMinutes);
        verificationCodeStore.markSent(email);

        log.info("[DEV] 验证码已生成: email={}, code={}", email, code);
    }

    @Transactional
    public LoginResponseVO login(String studentId, String verifyCode) {
        String email = buildEmail(studentId);

        if (!verificationCodeStore.verify(email, verifyCode)) {
            throw new BusinessException(400, "验证码错误或已过期");
        }

        User user = userRepository.findByStudentId(studentId).orElseGet(() -> {
            User newUser = new User();
            newUser.setStudentId(studentId);
            newUser.setEmail(email);
            newUser.setIsProfileComplete(false);
            return userRepository.save(newUser);
        });

        boolean isNewUser = !Boolean.TRUE.equals(user.getIsProfileComplete());
        String token = jwtUtil.generateToken(user.getId(), user.getStudentId());

        LoginResponseVO response = new LoginResponseVO();
        response.setToken(token);
        response.setIsNewUser(isNewUser);
        response.setUserId(user.getId());
        return response;
    }

    private String buildEmail(String studentId) {
        return studentId + "@seu.edu.cn";
    }

    private String generateCode() {
        if (devFixedCode != null && !devFixedCode.isBlank()) {
            return devFixedCode;
        }
        int value = secureRandom.nextInt(1_000_000);
        return String.format("%06d", value);
    }
}
