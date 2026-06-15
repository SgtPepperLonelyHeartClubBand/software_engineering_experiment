package Market_backend.config;

import Market_backend.entity.Location;
import Market_backend.entity.User;
import Market_backend.repository.LocationRepository;
import Market_backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initLocations(LocationRepository locationRepository) {
        return args -> {
            if (locationRepository.count() > 0) {
                return;
            }

            // 九龙湖校区
            Location jlh = saveLocation(locationRepository, null, "九龙湖校区", "JLH", 1);
            Location jlhMy = saveLocation(locationRepository, jlh.getId(), "梅园", "JLH-MY", 2);
            saveLocation(locationRepository, jlhMy.getId(), "梅园1栋", "JLH-MY-01", 3);
            saveLocation(locationRepository, jlhMy.getId(), "梅园2栋", "JLH-MY-02", 3);
            saveLocation(locationRepository, jlhMy.getId(), "梅园3栋", "JLH-MY-03", 3);

            Location jlhTy = saveLocation(locationRepository, jlh.getId(), "桃园", "JLH-TY", 2);
            saveLocation(locationRepository, jlhTy.getId(), "桃园1栋", "JLH-TY-01", 3);
            saveLocation(locationRepository, jlhTy.getId(), "桃园2栋", "JLH-TY-02", 3);

            Location jlhJy = saveLocation(locationRepository, jlh.getId(), "橘园", "JLH-JY", 2);
            saveLocation(locationRepository, jlhJy.getId(), "橘园1栋", "JLH-JY-01", 3);

            // 四牌楼校区
            Location spl = saveLocation(locationRepository, null, "四牌楼校区", "SPL", 1);
            Location splCxy = saveLocation(locationRepository, spl.getId(), "成贤院", "SPL-CXY", 2);
            saveLocation(locationRepository, splCxy.getId(), "成贤1舍", "SPL-CX-01", 3);
            saveLocation(locationRepository, splCxy.getId(), "成贤2舍", "SPL-CX-02", 3);

            Location splSty = saveLocation(locationRepository, spl.getId(), "沙塘园", "SPL-STY", 2);
            saveLocation(locationRepository, splSty.getId(), "沙塘园宿舍", "SPL-STY-01", 3);

            // 丁家桥校区
            Location djq = saveLocation(locationRepository, null, "丁家桥校区", "DJQ", 1);
            Location djqQef = saveLocation(locationRepository, djq.getId(), "求恩坊", "DJQ-QEF", 2);
            saveLocation(locationRepository, djqQef.getId(), "求恩1舍", "DJQ-QE-01", 3);
        };
    }

    private Location saveLocation(
            LocationRepository repository,
            Long parentId,
            String name,
            String code,
            int level
    ) {
        Location location = new Location();
        location.setParentId(parentId);
        location.setName(name);
        location.setCode(code);
        location.setLevel(level);
        return repository.save(location);
    }

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, LocationRepository locationRepository) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            Location location = locationRepository.findByCode("JLH-MY-01").orElse(null);

            User user = new User();
            user.setStudentId("220000001");
            user.setEmail("220000001@seu.edu.cn");
            user.setNickname("东大淘货王");
            user.setAvatarUrl("https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg");
            user.setWechat("seu_market_01");
            user.setLocation(location);
            user.setIsProfileComplete(true);
            userRepository.save(user);
        };
    }
}
