package Market_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SendCodeRequest {

    @NotBlank(message = "请输入一卡通号")
    @Pattern(regexp = "^[0-9]{9}$", message = "请输入有效的 9 位一卡通号")
    private String studentId;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}
