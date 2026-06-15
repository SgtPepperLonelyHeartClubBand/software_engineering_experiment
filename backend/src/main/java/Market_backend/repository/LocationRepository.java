package Market_backend.repository;

import Market_backend.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByParentId(Long parentId);

    List<Location> findByParentIdIsNull();

    Optional<Location> findByCode(String code);
}
