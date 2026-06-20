package Market_backend.service;

import Market_backend.common.BusinessException;
import Market_backend.dto.ItemListVO;
import Market_backend.dto.OrderVO;
import Market_backend.entity.Conversation;
import Market_backend.entity.Item;
import Market_backend.entity.SystemNotification;
import Market_backend.entity.TradeOrder;
import Market_backend.entity.User;
import Market_backend.repository.ItemRepository;
import Market_backend.repository.TradeOrderRepository;
import Market_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TradeService {

    private final TradeOrderRepository tradeOrderRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final NotificationService notificationService;
    private final ItemService itemService;

    public TradeService(
            TradeOrderRepository tradeOrderRepository,
            ItemRepository itemRepository,
            UserRepository userRepository,
            ConversationService conversationService,
            NotificationService notificationService,
            ItemService itemService
    ) {
        this.tradeOrderRepository = tradeOrderRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.conversationService = conversationService;
        this.notificationService = notificationService;
        this.itemService = itemService;
    }

    @Transactional
    public OrderVO reserve(Long userId, Long itemId) {
        User buyer = findUser(userId);
        Item item = findVisibleItemForUpdate(itemId);
        User seller = item.getSeller();

        if (seller.getId().equals(userId)) {
            throw new BusinessException(400, "不能预定自己发布的商品");
        }
        if (!Item.STATUS_ON_SALE.equals(item.getStatus())) {
            throw new BusinessException(400, "商品当前不可预定");
        }

        item.setStatus(Item.STATUS_RESERVED);
        item.setWantCount(nullToZero(item.getWantCount()) + 1);

        TradeOrder order = new TradeOrder();
        order.setItem(item);
        order.setBuyer(buyer);
        order.setSeller(seller);
        order.setStatus(TradeOrder.STATUS_RESERVED);
        TradeOrder saved = tradeOrderRepository.save(order);

        Conversation conversation = conversationService.getOrCreateConversation(item, buyer, seller);
        notificationService.create(
                seller,
                SystemNotification.TYPE_RESERVE_CREATED,
                "商品被预定",
                displayName(buyer) + " 预定了你的商品「" + item.getTitle() + "」",
                "cart-o",
                item,
                conversation
        );

        return toOrderVO(saved, conversation.getId());
    }

    @Transactional
    public OrderVO cancel(Long userId, Long orderId) {
        TradeOrder order = findOrderForUpdate(orderId);
        ensureParticipant(order, userId);
        if (!TradeOrder.STATUS_RESERVED.equals(order.getStatus())) {
            throw new BusinessException(400, "当前订单不能取消");
        }

        Item item = findVisibleItemForUpdate(order.getItem().getId());
        order.setStatus(TradeOrder.STATUS_CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        item.setStatus(Item.STATUS_ON_SALE);
        item.setWantCount(Math.max(0, nullToZero(item.getWantCount()) - 1));

        User actor = order.getBuyer().getId().equals(userId) ? order.getBuyer() : order.getSeller();
        User recipient = order.getBuyer().getId().equals(userId) ? order.getSeller() : order.getBuyer();
        Conversation conversation = conversationService.getOrCreateConversation(item, order.getBuyer(), order.getSeller());
        notificationService.create(
                recipient,
                SystemNotification.TYPE_RESERVE_CANCELLED,
                "预定已取消",
                displayName(actor) + " 取消了商品「" + item.getTitle() + "」的预定",
                "warning-o",
                item,
                conversation
        );

        return toOrderVO(order, conversation.getId());
    }

    @Transactional
    public OrderVO complete(Long userId, Long orderId) {
        TradeOrder order = findOrderForUpdate(orderId);
        if (!order.getSeller().getId().equals(userId)) {
            throw new BusinessException(403, "只有卖家可以确认交易完成");
        }
        if (!TradeOrder.STATUS_RESERVED.equals(order.getStatus())) {
            throw new BusinessException(400, "当前订单不能完成");
        }

        Item item = findVisibleItemForUpdate(order.getItem().getId());
        order.setStatus(TradeOrder.STATUS_COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        item.setStatus(Item.STATUS_COMPLETED);

        Conversation conversation = conversationService.getOrCreateConversation(item, order.getBuyer(), order.getSeller());
        notificationService.create(
                order.getBuyer(),
                SystemNotification.TYPE_ORDER_COMPLETED,
                "交易已完成",
                "卖家确认商品「" + item.getTitle() + "」交易完成",
                "passed",
                item,
                conversation
        );

        return toOrderVO(order, conversation.getId());
    }

    @Transactional(readOnly = true)
    public List<ItemListVO> listReserved(Long userId) {
        return tradeOrderRepository.findByBuyerIdAndStatusOrderByCreatedAtDesc(userId, TradeOrder.STATUS_RESERVED)
                .stream()
                .map(TradeOrder::getItem)
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .map(itemService::toListVO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItemListVO> listBought(Long userId) {
        return tradeOrderRepository.findByBuyerIdAndStatusOrderByCreatedAtDesc(userId, TradeOrder.STATUS_COMPLETED)
                .stream()
                .map(TradeOrder::getItem)
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .map(itemService::toListVO)
                .toList();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }

    private TradeOrder findOrderForUpdate(Long orderId) {
        return tradeOrderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
    }

    private Item findVisibleItemForUpdate(Long itemId) {
        Item item = itemRepository.findByIdForUpdate(itemId)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        if (Boolean.TRUE.equals(item.getDeleted())) {
            throw new BusinessException(404, "商品不存在");
        }
        return item;
    }

    private void ensureParticipant(TradeOrder order, Long userId) {
        if (!order.getBuyer().getId().equals(userId) && !order.getSeller().getId().equals(userId)) {
            throw new BusinessException(403, "只能操作自己的订单");
        }
    }

    private OrderVO toOrderVO(TradeOrder order, Long conversationId) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setItemId(order.getItem().getId());
        vo.setBuyerId(order.getBuyer().getId());
        vo.setSellerId(order.getSeller().getId());
        vo.setStatus(order.getStatus());
        vo.setStatusText(statusText(order.getStatus()));
        vo.setItemStatus(order.getItem().getStatus());
        vo.setConversationId(conversationId);
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());
        return vo;
    }

    private String statusText(String status) {
        return switch (status) {
            case TradeOrder.STATUS_RESERVED -> "已预定";
            case TradeOrder.STATUS_CANCELLED -> "已取消";
            case TradeOrder.STATUS_COMPLETED -> "已完成";
            default -> status;
        };
    }

    private String displayName(User user) {
        return user.getNickname() == null || user.getNickname().isBlank() ? user.getStudentId() : user.getNickname();
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }
}
