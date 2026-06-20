package Market_backend.repository;

import Market_backend.entity.Item;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Item i where i.id = :id")
    Optional<Item> findByIdForUpdate(Long id);
}
