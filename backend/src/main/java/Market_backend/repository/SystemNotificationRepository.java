package Market_backend.repository;

import Market_backend.entity.SystemNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {

    List<SystemNotification> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndReadFalse(Long userId);
}
