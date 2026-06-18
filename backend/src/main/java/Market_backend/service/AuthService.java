package Market_backend.service;

import Market_backend.common.BusinessException;
import Market_backend.dto.LoginResponseVO;
import Market_backend.entity.User;
import Market_backend.repository.UserRepository;
import Market_backend.config.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CampusIdentityProvider campusIdentityProvider;

    public AuthService(
            UserRepository userRepository,
            JwtUtil jwtUtil,
            CampusIdentityProvider campusIdentityProvider
    ) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.campusIdentityProvider = campusIdentityProvider;
    }

    public void sendCode(String studentId) {
        campusIdentityProvider.sendCode(studentId);
    }

    @Transactional
    public LoginResponseVO login(String studentId, String verifyCode) {
        if (!campusIdentityProvider.verifyCode(studentId, verifyCode)) {
            throw new BusinessException(400, "验证码错误或已过期");
        }

        User user = userRepository.findByStudentId(studentId).orElseGet(() -> {
            User newUser = new User();
            newUser.setStudentId(studentId);
            newUser.setEmail(campusIdentityProvider.buildEmail(studentId));
            newUser.setIsProfileComplete(false);
            return userRepository.save(newUser);
        });

        boolean isNewUser = !Boolean.TRUE.equals(user.getIsProfileComplete());
        String token = jwtUtil.generateToken(user.getId(), user.getStudentId());

        LoginResponseVO response = new LoginResponseVO();
        response.setToken(token);
        response.setIsNewUser(isNewUser);
        response.setUserId(user.getId());
        log.info("用户登录成功: userId={}, studentId={}", user.getId(), user.getStudentId());
        return response;
    }
}
