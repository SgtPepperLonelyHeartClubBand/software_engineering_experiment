package Market_backend.repository;

import Market_backend.entity.TradeOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long> {

    Optional<TradeOrder> findByItemIdAndStatus(Long itemId, String status);

    List<TradeOrder> findByBuyerIdAndStatusOrderByCreatedAtDesc(Long buyerId, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from TradeOrder o where o.id = :id")
    Optional<TradeOrder> findByIdForUpdate(Long id);
}
