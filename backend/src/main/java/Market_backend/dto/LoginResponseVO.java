package Market_backend.dto;

public class LoginResponseVO {

    private String token;
    private Boolean isNewUser;
    private Long userId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getIsNewUser() {
        return isNewUser;
    }

    public void setIsNewUser(Boolean isNewUser) {
        this.isNewUser = isNewUser;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
