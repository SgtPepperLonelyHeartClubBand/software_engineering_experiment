package Market_backend.repository;

import Market_backend.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    Optional<Favorite> findByUserIdAndItemId(Long userId, Long itemId);

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);
}
