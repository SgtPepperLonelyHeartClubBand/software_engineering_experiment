package Market_backend.service;

import Market_backend.common.BusinessException;
import Market_backend.dto.NotificationVO;
import Market_backend.entity.Conversation;
import Market_backend.entity.Item;
import Market_backend.entity.SystemNotification;
import Market_backend.entity.User;
import Market_backend.repository.SystemNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final SystemNotificationRepository notificationRepository;

    public NotificationService(SystemNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void create(
            User user,
            String type,
            String title,
            String content,
            String icon,
            Item item,
            Conversation conversation
    ) {
        SystemNotification notification = new SystemNotification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIcon(icon);
        notification.setItem(item);
        notification.setConversation(conversation);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationVO> list(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toVO)
                .toList();
    }

    @Transactional
    public void markRead(Long userId, Long notificationId) {
        SystemNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(404, "通知不存在"));
        if (!notification.getUser().getId().equals(userId)) {
            throw new BusinessException(403, "只能操作自己的通知");
        }
        markRead(notification);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .forEach(this::markRead);
    }

    @Transactional(readOnly = true)
    public int countUnread(Long userId) {
        return Math.toIntExact(notificationRepository.countByUserIdAndReadFalse(userId));
    }

    private void markRead(SystemNotification notification) {
        if (Boolean.TRUE.equals(notification.getRead())) {
            return;
        }
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
    }

    private NotificationVO toVO(SystemNotification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setType(notification.getType());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setIcon(notification.getIcon());
        vo.setItemId(notification.getItem() == null ? null : notification.getItem().getId());
        vo.setConversationId(notification.getConversation() == null ? null : notification.getConversation().getId());
        vo.setTime(notification.getCreatedAt());
        vo.setUnread(!Boolean.TRUE.equals(notification.getRead()));
        return vo;
    }
}
