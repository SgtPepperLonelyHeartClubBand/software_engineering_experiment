package Market_backend.controller;

import Market_backend.common.BusinessException;
import Market_backend.common.Result;
import Market_backend.common.UserContext;
import Market_backend.dto.UserProfileUpdateRequest;
import Market_backend.dto.UserProfileVO;
import Market_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public Result<UserProfileVO> me() {
        return Result.ok(userService.getCurrentUser(resolveUserId()));
    }

    @PutMapping("/me")
    public Result<UserProfileVO> updateMe(@Valid @RequestBody UserProfileUpdateRequest request) {
        return Result.ok(userService.updateCurrentUser(resolveUserId(), request));
    }

    private Long resolveUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }
}
