package Market_backend.service;

import Market_backend.common.BusinessException;
import Market_backend.dto.LocationNodeVO;
import Market_backend.dto.UserProfileUpdateRequest;
import Market_backend.dto.UserProfileVO;
import Market_backend.entity.Location;
import Market_backend.entity.User;
import Market_backend.repository.LocationRepository;
import Market_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        vo.setLocationCode(user.getLocation() == null ? null : user.getLocation().getCode());
        vo.setLocationText(buildLocationPath(user.getLocation()));
        vo.setIsProfileComplete(user.getIsProfileComplete());
        return vo;
    }

    @Transactional
    public UserProfileVO updateCurrentUser(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        Location location = locationRepository.findByCode(request.getLocationCode())
                .orElseThrow(() -> new BusinessException(400, "面交地点不存在"));

        user.setNickname(normalizeBlank(request.getNickname()));
        user.setWechat(normalizeBlank(request.getWechat()));
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(normalizeBlank(request.getAvatarUrl()));
        }
        user.setLocation(location);
        user.setIsProfileComplete(true);
        return toProfileVO(user);
    }

    @Transactional(readOnly = true)
    public UserProfileVO getDevCurrentUser() {
        return getCurrentUser(DEV_DEFAULT_USER_ID);
    }

    @Transactional(readOnly = true)
    public List<LocationNodeVO> getLocationTree() {
        List<Location> locations = locationRepository.findAll().stream()
                .sorted(Comparator.comparing(Location::getLevel).thenComparing(Location::getId))
                .toList();
        Map<Long, List<Location>> childrenByParentId = new HashMap<>();
        for (Location location : locations) {
            childrenByParentId
                    .computeIfAbsent(location.getParentId(), ignored -> new ArrayList<>())
                    .add(location);
        }
        return buildLocationNodes(childrenByParentId, null);
    }

    private UserProfileVO toProfileVO(User user) {
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setStudentId(user.getStudentId());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setWechat(user.getWechat());
        vo.setLocationCode(user.getLocation() == null ? null : user.getLocation().getCode());
        vo.setLocationText(buildLocationPath(user.getLocation()));
        vo.setIsProfileComplete(user.getIsProfileComplete());
        return vo;
    }

    private List<LocationNodeVO> buildLocationNodes(Map<Long, List<Location>> childrenByParentId, Long parentId) {
        return childrenByParentId.getOrDefault(parentId, List.of()).stream()
                .map(location -> {
                    LocationNodeVO node = new LocationNodeVO();
                    node.setId(location.getId());
                    node.setText(location.getName());
                    node.setValue(location.getCode());
                    node.setLevel(location.getLevel());
                    node.setChildren(buildLocationNodes(childrenByParentId, location.getId()));
                    return node;
                })
                .toList();
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
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
