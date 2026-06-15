package Market_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LoginRequest {

    @NotBlank(message = "请输入一卡通号")
    @Pattern(regexp = "^[0-9]{9}$", message = "请输入有效的 9 位一卡通号")
    private String studentId;

    @NotBlank(message = "请输入验证码")
    @Pattern(regexp = "^[0-9]{4,6}$", message = "验证码格式不正确")
    private String verifyCode;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }
}
