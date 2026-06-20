package Market_backend.service;

import Market_backend.common.BusinessException;
import Market_backend.dto.ItemListVO;
import Market_backend.entity.Favorite;
import Market_backend.entity.Item;
import Market_backend.entity.User;
import Market_backend.repository.FavoriteRepository;
import Market_backend.repository.ItemRepository;
import Market_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemService itemService;

    public FavoriteService(
            FavoriteRepository favoriteRepository,
            ItemRepository itemRepository,
            UserRepository userRepository,
            ItemService itemService
    ) {
        this.favoriteRepository = favoriteRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.itemService = itemService;
    }

    @Transactional
    public void favorite(Long userId, Long itemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        Item item = findVisibleItem(itemId);
        if (item.getSeller().getId().equals(userId)) {
            throw new BusinessException(400, "不能收藏自己发布的商品");
        }
        if (favoriteRepository.existsByUserIdAndItemId(userId, itemId)) {
            return;
        }
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setItem(item);
        favoriteRepository.save(favorite);
    }

    @Transactional
    public void unfavorite(Long userId, Long itemId) {
        favoriteRepository.findByUserIdAndItemId(userId, itemId)
                .ifPresent(favoriteRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<ItemListVO> listFavorites(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(Favorite::getItem)
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .map(itemService::toListVO)
                .toList();
    }

    private Item findVisibleItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        if (Boolean.TRUE.equals(item.getDeleted())) {
            throw new BusinessException(404, "商品不存在");
        }
        return item;
    }
}
