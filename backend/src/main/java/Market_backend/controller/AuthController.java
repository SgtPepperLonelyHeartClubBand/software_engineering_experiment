package Market_backend.controller;

import Market_backend.common.Result;
import Market_backend.dto.LoginRequest;
import Market_backend.dto.LoginResponseVO;
import Market_backend.dto.SendCodeRequest;
import Market_backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        authService.sendCode(request.getStudentId());
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<LoginResponseVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request.getStudentId(), request.getVerifyCode()));
    }
}
