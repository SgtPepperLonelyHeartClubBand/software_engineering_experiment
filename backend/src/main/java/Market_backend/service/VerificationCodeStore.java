package Market_backend.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VerificationCodeStore {

    private static final class CodeEntry {
        private final String code;
        private final Instant expiresAt;

        private CodeEntry(String code, Instant expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<String, CodeEntry> codes = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastSentAt = new ConcurrentHashMap<>();

    public void save(String email, String code, long expirationMinutes) {
        codes.put(email, new CodeEntry(code, Instant.now().plusSeconds(expirationMinutes * 60)));
    }

    public boolean verify(String email, String inputCode) {
        CodeEntry entry = codes.get(email);
        if (entry == null) {
            return false;
        }
        if (Instant.now().isAfter(entry.expiresAt)) {
            codes.remove(email);
            return false;
        }
        boolean matched = entry.code.equals(inputCode);
        if (matched) {
            codes.remove(email);
        }
        return matched;
    }

    public boolean canSend(String email, long intervalSeconds) {
        Instant lastSent = lastSentAt.get(email);
        if (lastSent == null) {
            return true;
        }
        return Instant.now().isAfter(lastSent.plusSeconds(intervalSeconds));
    }

    public void markSent(String email) {
        lastSentAt.put(email, Instant.now());
    }
}
