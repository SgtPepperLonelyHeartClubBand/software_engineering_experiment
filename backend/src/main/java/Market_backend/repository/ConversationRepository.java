package Market_backend.repository;

import Market_backend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByItemIdAndBuyerIdAndSellerId(Long itemId, Long buyerId, Long sellerId);

    @Query("""
            select c from Conversation c
            where c.buyer.id = :userId or c.seller.id = :userId
            """)
    List<Conversation> findByParticipantId(Long userId);

    @Query("""
            select c from Conversation c
            where (c.buyer.id = :userId and c.buyerHidden = false)
               or (c.seller.id = :userId and c.sellerHidden = false)
            """)
    List<Conversation> findVisibleByParticipantId(Long userId);
}
