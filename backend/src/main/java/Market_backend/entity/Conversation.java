package Market_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "conversations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_conversations_item_buyer_seller",
                columnNames = {"item_id", "buyer_id", "seller_id"}
        )
)
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(name = "last_message", length = 220)
    private String lastMessage;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "buyer_unread_count", nullable = false)
    private Integer buyerUnreadCount = 0;

    @Column(name = "seller_unread_count", nullable = false)
    private Integer sellerUnreadCount = 0;

    @Column(name = "buyer_hidden", nullable = false)
    private Boolean buyerHidden = false;

    @Column(name = "seller_hidden", nullable = false)
    private Boolean sellerHidden = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public Integer getBuyerUnreadCount() {
        return buyerUnreadCount;
    }

    public void setBuyerUnreadCount(Integer buyerUnreadCount) {
        this.buyerUnreadCount = buyerUnreadCount;
    }

    public Integer getSellerUnreadCount() {
        return sellerUnreadCount;
    }

    public void setSellerUnreadCount(Integer sellerUnreadCount) {
        this.sellerUnreadCount = sellerUnreadCount;
    }

    public Boolean getBuyerHidden() {
        return buyerHidden;
    }

    public void setBuyerHidden(Boolean buyerHidden) {
        this.buyerHidden = buyerHidden;
    }

    public Boolean getSellerHidden() {
        return sellerHidden;
    }

    public void setSellerHidden(Boolean sellerHidden) {
        this.sellerHidden = sellerHidden;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
