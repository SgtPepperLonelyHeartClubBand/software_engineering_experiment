package Market_backend.dto;

import java.time.LocalDateTime;

public class ChatMessageVO {

    private Long id;
    private String content;
    private Boolean isSelf;
    private Long senderId;
    private String senderName;
    private LocalDateTime timestamp;
    private Boolean recalled;
    private QuoteVO quote;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsSelf() {
        return isSelf;
    }

    public void setIsSelf(Boolean isSelf) {
        this.isSelf = isSelf;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getRecalled() {
        return recalled;
    }

    public void setRecalled(Boolean recalled) {
        this.recalled = recalled;
    }

    public QuoteVO getQuote() {
        return quote;
    }

    public void setQuote(QuoteVO quote) {
        this.quote = quote;
    }

    public static class QuoteVO {

        private Long id;
        private String content;
        private Boolean isSelf;
        private String senderName;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Boolean getIsSelf() {
            return isSelf;
        }

        public void setIsSelf(Boolean isSelf) {
            this.isSelf = isSelf;
        }

        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }
    }
}
