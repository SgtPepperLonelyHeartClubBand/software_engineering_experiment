package Market_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatMessageRequest {

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 200, message = "消息最多 200 个字符")
    private String content;

    private Long quoteMessageId;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getQuoteMessageId() {
        return quoteMessageId;
    }

    public void setQuoteMessageId(Long quoteMessageId) {
        this.quoteMessageId = quoteMessageId;
    }
}
