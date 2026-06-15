package Market_backend.controller;

import Market_backend.common.Result;
import Market_backend.common.UserContext;
import Market_backend.dto.UserProfileVO;
import Market_backend.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public Result<UserProfileVO> me(
            @RequestHeader(value = "X-Dev-User-Id", required = false) Long devUserId
    ) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            userId = devUserId != null ? devUserId : 1L;
        }
        return Result.ok(userService.getCurrentUser(userId));
    }
}
