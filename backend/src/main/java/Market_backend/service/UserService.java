package Market_backend.service;

import Market_backend.common.BusinessException;
import Market_backend.dto.UserProfileVO;
import Market_backend.entity.Location;
import Market_backend.entity.User;
import Market_backend.repository.LocationRepository;
import Market_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UserService {

    private static final long DEV_DEFAULT_USER_ID = 1L;

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    public UserService(UserRepository userRepository, LocationRepository locationRepository) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileVO getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setStudentId(user.getStudentId());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setWechat(user.getWechat());
        vo.setLocationText(buildLocationPath(user.getLocation()));
        vo.setIsProfileComplete(user.getIsProfileComplete());
        return vo;
    }

    @Transactional(readOnly = true)
    public UserProfileVO getDevCurrentUser() {
        return getCurrentUser(DEV_DEFAULT_USER_ID);
    }

    private String buildLocationPath(Location location) {
        if (location == null) {
            return null;
        }

        List<String> names = new ArrayList<>();
        Location current = location;
        while (current != null) {
            names.add(current.getName());
            if (current.getParentId() == null) {
                break;
            }
            current = locationRepository.findById(current.getParentId()).orElse(null);
        }
        Collections.reverse(names);
        return String.join(" / ", names);
    }
}
