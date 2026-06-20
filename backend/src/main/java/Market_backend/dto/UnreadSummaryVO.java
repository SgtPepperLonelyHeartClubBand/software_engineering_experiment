package Market_backend.dto;

public class UnreadSummaryVO {

    private Integer chatUnread;
    private Integer notificationUnread;
    private Integer totalUnread;

    public Integer getChatUnread() {
        return chatUnread;
    }

    public void setChatUnread(Integer chatUnread) {
        this.chatUnread = chatUnread;
    }

    public Integer getNotificationUnread() {
        return notificationUnread;
    }

    public void setNotificationUnread(Integer notificationUnread) {
        this.notificationUnread = notificationUnread;
    }

    public Integer getTotalUnread() {
        return totalUnread;
    }

    public void setTotalUnread(Integer totalUnread) {
        this.totalUnread = totalUnread;
    }
}
