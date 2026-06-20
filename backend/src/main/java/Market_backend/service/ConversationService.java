package Market_backend.service;

import Market_backend.common.BusinessException;
import Market_backend.dto.ChatMessageRequest;
import Market_backend.dto.ChatMessageVO;
import Market_backend.dto.ConversationVO;
import Market_backend.dto.UnreadSummaryVO;
import Market_backend.entity.ChatMessage;
import Market_backend.entity.Conversation;
import Market_backend.entity.Item;
import Market_backend.entity.User;
import Market_backend.repository.ChatMessageRepository;
import Market_backend.repository.ConversationRepository;
import Market_backend.repository.ItemRepository;
import Market_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ConversationService {

    private static final String DEFAULT_AVATAR_URL = "https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg";
    private static final Duration RECALL_WINDOW = Duration.ofMinutes(2);

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ConversationService(
            ConversationRepository conversationRepository,
            ChatMessageRepository chatMessageRepository,
            ItemRepository itemRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ConversationVO ensureConversation(Long userId, Long itemId) {
        User buyer = findUser(userId);
        Item item = findVisibleItem(itemId);
        User seller = item.getSeller();
        if (seller.getId().equals(userId)) {
            throw new BusinessException(400, "不能和自己发起私信");
        }
        Conversation conversation = getOrCreateConversation(item, buyer, seller);
        return toConversationVO(conversation, userId);
    }

    @Transactional
    public Conversation getOrCreateConversation(Item item, User buyer, User seller) {
        return conversationRepository.findByItemIdAndBuyerIdAndSellerId(item.getId(), buyer.getId(), seller.getId())
                .orElseGet(() -> {
                    Conversation conversation = new Conversation();
                    conversation.setItem(item);
                    conversation.setBuyer(buyer);
                    conversation.setSeller(seller);
                    conversation.setLastMessage("可以开始沟通面交细节");
                    conversation.setLastMessageAt(LocalDateTime.now());
                    return conversationRepository.save(conversation);
                });
    }

    @Transactional(readOnly = true)
    public List<ConversationVO> list(Long userId) {
        return conversationRepository.findVisibleByParticipantId(userId).stream()
                .sorted(Comparator
                        .comparing((Conversation c) -> unreadCount(c, userId) > 0).reversed()
                        .thenComparing(this::lastTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(conversation -> toConversationVO(conversation, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageVO> listMessages(Long userId, Long conversationId) {
        Conversation conversation = findConversationForUser(conversationId, userId);
        return chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId()).stream()
                .map(message -> toMessageVO(message, userId))
                .toList();
    }

    @Transactional
    public ChatMessageVO sendMessage(Long userId, Long conversationId, ChatMessageRequest request) {
        Conversation conversation = findConversationForUser(conversationId, userId);
        User sender = currentParticipant(conversation, userId);
        User recipient = otherParticipant(conversation, userId);

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(request.getContent().trim());
        message.setCreatedAt(LocalDateTime.now());
        if (request.getQuoteMessageId() != null) {
            ChatMessage quote = chatMessageRepository.findById(request.getQuoteMessageId())
                    .orElseThrow(() -> new BusinessException(404, "引用消息不存在"));
            if (!quote.getConversation().getId().equals(conversation.getId())) {
                throw new BusinessException(400, "只能引用当前会话中的消息");
            }
            if (Boolean.TRUE.equals(quote.getRecalled())) {
                throw new BusinessException(400, "不能引用已撤回的消息");
            }
            message.setQuoteMessage(quote);
        }

        ChatMessage saved = chatMessageRepository.save(message);
        conversation.setLastMessage(saved.getContent());
        conversation.setLastMessageAt(LocalDateTime.now());
        if (conversation.getBuyer().getId().equals(recipient.getId())) {
            conversation.setBuyerUnreadCount(nullToZero(conversation.getBuyerUnreadCount()) + 1);
            conversation.setBuyerHidden(false);
        } else {
            conversation.setSellerUnreadCount(nullToZero(conversation.getSellerUnreadCount()) + 1);
            conversation.setSellerHidden(false);
        }
        if (conversation.getBuyer().getId().equals(sender.getId())) {
            conversation.setBuyerHidden(false);
        } else {
            conversation.setSellerHidden(false);
        }
        return toMessageVO(saved, userId);
    }

    @Transactional
    public void markRead(Long userId, Long conversationId) {
        Conversation conversation = findConversationForUser(conversationId, userId);
        if (conversation.getBuyer().getId().equals(userId)) {
            conversation.setBuyerUnreadCount(0);
        } else {
            conversation.setSellerUnreadCount(0);
        }
    }

    @Transactional
    public void deleteConversation(Long userId, Long conversationId) {
        Conversation conversation = findConversationForUser(conversationId, userId);
        if (conversation.getBuyer().getId().equals(userId)) {
            conversation.setBuyerHidden(true);
            conversation.setBuyerUnreadCount(0);
        } else {
            conversation.setSellerHidden(true);
            conversation.setSellerUnreadCount(0);
        }
    }

    @Transactional
    public ChatMessageVO recallMessage(Long userId, Long messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(404, "消息不存在"));
        if (!message.getSender().getId().equals(userId)) {
            throw new BusinessException(403, "只能撤回自己发送的消息");
        }
        if (message.getCreatedAt() == null || Duration.between(message.getCreatedAt(), LocalDateTime.now()).compareTo(RECALL_WINDOW) > 0) {
            throw new BusinessException(400, "超过 2 分钟，无法撤回");
        }
        message.setRecalled(true);
        message.setContent("");
        Conversation conversation = message.getConversation();
        conversation.setLastMessage("一条消息被撤回");
        conversation.setLastMessageAt(LocalDateTime.now());
        return toMessageVO(message, userId);
    }

    @Transactional(readOnly = true)
    public UnreadSummaryVO unreadSummary(Long userId) {
        int chatUnread = conversationRepository.findByParticipantId(userId).stream()
                .mapToInt(conversation -> unreadCount(conversation, userId))
                .sum();
        int notificationUnread = notificationService.countUnread(userId);
        UnreadSummaryVO vo = new UnreadSummaryVO();
        vo.setChatUnread(chatUnread);
        vo.setNotificationUnread(notificationUnread);
        vo.setTotalUnread(chatUnread + notificationUnread);
        return vo;
    }

    private Conversation findConversationForUser(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(404, "会话不存在"));
        if (!isParticipant(conversation, userId)) {
            throw new BusinessException(403, "只能查看自己的会话");
        }
        return conversation;
    }

    private Item findVisibleItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        if (Boolean.TRUE.equals(item.getDeleted())) {
            throw new BusinessException(404, "商品不存在");
        }
        return item;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }

    private boolean isParticipant(Conversation conversation, Long userId) {
        return conversation.getBuyer().getId().equals(userId) || conversation.getSeller().getId().equals(userId);
    }

    private User currentParticipant(Conversation conversation, Long userId) {
        if (conversation.getBuyer().getId().equals(userId)) {
            return conversation.getBuyer();
        }
        if (conversation.getSeller().getId().equals(userId)) {
            return conversation.getSeller();
        }
        throw new BusinessException(403, "只能操作自己的会话");
    }

    private User otherParticipant(Conversation conversation, Long userId) {
        if (conversation.getBuyer().getId().equals(userId)) {
            return conversation.getSeller();
        }
        if (conversation.getSeller().getId().equals(userId)) {
            return conversation.getBuyer();
        }
        throw new BusinessException(403, "只能操作自己的会话");
    }

    private ConversationVO toConversationVO(Conversation conversation, Long userId) {
        User other = otherParticipant(conversation, userId);
        Item item = conversation.getItem();
        ConversationVO vo = new ConversationVO();
        vo.setId(conversation.getId());
        vo.setName(displayName(other));
        vo.setAvatar(isBlank(other.getAvatarUrl()) ? DEFAULT_AVATAR_URL : other.getAvatarUrl());
        vo.setItemId(item.getId());
        vo.setItemTitle(item.getTitle());
        vo.setItemPrice(item.getPrice());
        vo.setItemImage(item.getCoverImageUrl());
        vo.setLastMessage(isBlank(conversation.getLastMessage()) ? "暂无消息" : conversation.getLastMessage());
        vo.setTime(lastTime(conversation));
        vo.setUnread(unreadCount(conversation, userId));
        return vo;
    }

    private ChatMessageVO toMessageVO(ChatMessage message, Long userId) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setContent(message.getContent());
        vo.setIsSelf(message.getSender().getId().equals(userId));
        vo.setSenderId(message.getSender().getId());
        vo.setSenderName(displayName(message.getSender()));
        vo.setTimestamp(message.getCreatedAt());
        vo.setRecalled(Boolean.TRUE.equals(message.getRecalled()));
        if (message.getQuoteMessage() != null) {
            ChatMessage quote = message.getQuoteMessage();
            ChatMessageVO.QuoteVO quoteVO = new ChatMessageVO.QuoteVO();
            quoteVO.setId(quote.getId());
            quoteVO.setContent(quote.getContent());
            quoteVO.setIsSelf(quote.getSender().getId().equals(userId));
            quoteVO.setSenderName(quote.getSender().getId().equals(userId) ? "我" : displayName(quote.getSender()));
            vo.setQuote(quoteVO);
        }
        return vo;
    }

    private LocalDateTime lastTime(Conversation conversation) {
        if (conversation.getLastMessageAt() != null) {
            return conversation.getLastMessageAt();
        }
        return conversation.getUpdatedAt() == null ? conversation.getCreatedAt() : conversation.getUpdatedAt();
    }

    private int unreadCount(Conversation conversation, Long userId) {
        if (conversation.getBuyer().getId().equals(userId)) {
            return nullToZero(conversation.getBuyerUnreadCount());
        }
        if (conversation.getSeller().getId().equals(userId)) {
            return nullToZero(conversation.getSellerUnreadCount());
        }
        return 0;
    }

    private String displayName(User user) {
        return isBlank(user.getNickname()) ? user.getStudentId() : user.getNickname();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }
}
