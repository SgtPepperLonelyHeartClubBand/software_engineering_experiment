package Market_backend.service;

import Market_backend.common.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class VerificationCodeCampusIdentityProvider implements CampusIdentityProvider {

    private static final Logger log = LoggerFactory.getLogger(VerificationCodeCampusIdentityProvider.class);

    private final VerificationCodeStore verificationCodeStore;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${auth.dev-fixed-code:123456}")
    private String devFixedCode;

    @Value("${auth.code-expiration-minutes:5}")
    private long codeExpirationMinutes;

    @Value("${auth.send-interval-seconds:60}")
    private long sendIntervalSeconds;

    public VerificationCodeCampusIdentityProvider(VerificationCodeStore verificationCodeStore) {
        this.verificationCodeStore = verificationCodeStore;
    }

    @Override
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

    @Override
    public boolean verifyCode(String studentId, String verifyCode) {
        return verificationCodeStore.verify(buildEmail(studentId), verifyCode);
    }

    @Override
    public String buildEmail(String studentId) {
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
