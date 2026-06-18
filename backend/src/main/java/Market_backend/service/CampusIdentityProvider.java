package Market_backend.service;

public interface CampusIdentityProvider {

    void sendCode(String studentId);

    boolean verifyCode(String studentId, String verifyCode);

    String buildEmail(String studentId);
}
