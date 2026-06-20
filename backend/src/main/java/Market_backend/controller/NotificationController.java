package Market_backend.controller;

import Market_backend.common.BusinessException;
import Market_backend.common.Result;
import Market_backend.common.UserContext;
import Market_backend.dto.NotificationVO;
import Market_backend.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Result<List<NotificationVO>> list() {
        return Result.ok(notificationService.list(resolveUserId()));
    }

    @PostMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(resolveUserId(), id);
        return Result.ok();
    }

    @PostMapping("/read-all")
    public Result<Void> markAllRead() {
        notificationService.markAllRead(resolveUserId());
        return Result.ok();
    }

    private Long resolveUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }
}
