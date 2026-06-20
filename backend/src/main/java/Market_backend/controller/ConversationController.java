package Market_backend.controller;

import Market_backend.common.BusinessException;
import Market_backend.common.Result;
import Market_backend.common.UserContext;
import Market_backend.dto.ChatMessageRequest;
import Market_backend.dto.ChatMessageVO;
import Market_backend.dto.ConversationRequest;
import Market_backend.dto.ConversationVO;
import Market_backend.dto.UnreadSummaryVO;
import Market_backend.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping("/conversations")
    public Result<ConversationVO> ensureConversation(@Valid @RequestBody ConversationRequest request) {
        return Result.ok(conversationService.ensureConversation(resolveUserId(), request.getItemId()));
    }

    @GetMapping("/conversations")
    public Result<List<ConversationVO>> conversations() {
        return Result.ok(conversationService.list(resolveUserId()));
    }

    @DeleteMapping("/conversations/{id}")
    public Result<Void> deleteConversation(@PathVariable Long id) {
        conversationService.deleteConversation(resolveUserId(), id);
        return Result.ok();
    }

    @GetMapping("/conversations/{id}/messages")
    public Result<List<ChatMessageVO>> messages(@PathVariable Long id) {
        return Result.ok(conversationService.listMessages(resolveUserId(), id));
    }

    @PostMapping("/conversations/{id}/messages")
    public Result<ChatMessageVO> sendMessage(
            @PathVariable Long id,
            @Valid @RequestBody ChatMessageRequest request
    ) {
        return Result.ok(conversationService.sendMessage(resolveUserId(), id, request));
    }

    @PostMapping("/conversations/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        conversationService.markRead(resolveUserId(), id);
        return Result.ok();
    }

    @PostMapping("/messages/{id}/recall")
    public Result<ChatMessageVO> recall(@PathVariable Long id) {
        return Result.ok(conversationService.recallMessage(resolveUserId(), id));
    }

    @GetMapping("/messages/unread-summary")
    public Result<UnreadSummaryVO> unreadSummary() {
        return Result.ok(conversationService.unreadSummary(resolveUserId()));
    }

    private Long resolveUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }
}
