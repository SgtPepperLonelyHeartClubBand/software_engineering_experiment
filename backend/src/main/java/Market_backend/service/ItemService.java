package Market_backend.service;

import Market_backend.common.BusinessException;
import Market_backend.dto.ItemDetailVO;
import Market_backend.dto.ItemListVO;
import Market_backend.dto.ItemRequest;
import Market_backend.entity.Item;
import Market_backend.entity.ItemImage;
import Market_backend.entity.Location;
import Market_backend.entity.User;
import Market_backend.repository.ItemRepository;
import Market_backend.repository.LocationRepository;
import Market_backend.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class ItemService {

    private static final String DEFAULT_AVATAR_URL = "https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg";

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    public ItemService(
            ItemRepository itemRepository,
            UserRepository userRepository,
            LocationRepository locationRepository
    ) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
    }

    @Transactional(readOnly = true)
    public List<ItemListVO> listItems(String category, String keyword, String status) {
        return itemRepository.findAll(buildListSpec(category, keyword, status), Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(this::toListVO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItemListVO> listMyItems(Long userId) {
        return itemRepository.findAll((root, query, cb) -> cb.and(
                        cb.isFalse(root.get("deleted")),
                        cb.equal(root.get("seller").get("id"), userId)
                ), Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toListVO)
                .toList();
    }

    @Transactional
    public ItemDetailVO getItemDetail(Long id) {
        Item item = findVisibleItem(id);
        item.setViewCount(nullToZero(item.getViewCount()) + 1);
        return toDetailVO(item);
    }

    @Transactional
    public ItemDetailVO createItem(Long userId, ItemRequest request) {
        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        if (!Boolean.TRUE.equals(seller.getIsProfileComplete())) {
            throw new BusinessException(403, "请先完善个人资料");
        }

        Item item = new Item();
        item.setSeller(seller);
        applyRequest(item, request);
        return toDetailVO(itemRepository.save(item));
    }

    @Transactional
    public ItemDetailVO updateItem(Long userId, Long itemId, ItemRequest request) {
        Item item = findVisibleItem(itemId);
        ensureOwner(item, userId);
        applyRequest(item, request);
        return toDetailVO(item);
    }

    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        Item item = findVisibleItem(itemId);
        ensureOwner(item, userId);
        item.setStatus(Item.STATUS_OFF_SHELF);
        item.setDeleted(true);
    }

    private Specification<Item> buildListSpec(String category, String keyword, String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            if (isBlank(status)) {
                predicates.add(cb.equal(root.get("status"), Item.STATUS_ON_SALE));
            } else {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            if (!isBlank(category)) {
                predicates.add(cb.equal(root.get("category"), category.trim()));
            }
            if (!isBlank(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Join<Item, User> seller = root.join("seller", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title").as(String.class)), pattern),
                        cb.like(cb.lower(root.get("description").as(String.class)), pattern),
                        cb.like(cb.lower(seller.get("nickname").as(String.class)), pattern)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Item findVisibleItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        if (Boolean.TRUE.equals(item.getDeleted())) {
            throw new BusinessException(404, "商品不存在");
        }
        return item;
    }

    private void applyRequest(Item item, ItemRequest request) {
        Location location = locationRepository.findByCode(request.getLocationCode())
                .orElseThrow(() -> new BusinessException(400, "面交地点不存在"));

        item.setTitle(request.getTitle().trim());
        item.setCategory(request.getCategory().trim());
        item.setCondition(request.getCondition().trim());
        item.setPrice(request.getPrice());
        item.setLocation(location);
        item.setDescription(normalizeBlank(request.getDescription()));
        item.setCoverImageUrl(request.getImageUrls().get(0).trim());
        replaceImages(item, request.getImageUrls());
    }

    private void replaceImages(Item item, List<String> imageUrls) {
        item.getImages().clear();
        for (int i = 0; i < imageUrls.size(); i++) {
            ItemImage image = new ItemImage();
            image.setItem(item);
            image.setUrl(imageUrls.get(i).trim());
            image.setSortOrder(i);
            item.getImages().add(image);
        }
    }

    private void ensureOwner(Item item, Long userId) {
        if (!item.getSeller().getId().equals(userId)) {
            throw new BusinessException(403, "只能操作自己发布的商品");
        }
    }

    private ItemListVO toListVO(Item item) {
        ItemListVO vo = new ItemListVO();
        vo.setId(item.getId());
        vo.setTitle(item.getTitle());
        vo.setPrice(item.getPrice());
        vo.setCondition(item.getCondition());
        vo.setCategory(item.getCategory());
        vo.setImage(item.getCoverImageUrl());
        vo.setSellerName(buildSellerName(item.getSeller()));
        vo.setSellerAvatar(isBlank(item.getSeller().getAvatarUrl()) ? DEFAULT_AVATAR_URL : item.getSeller().getAvatarUrl());
        vo.setWantCount(nullToZero(item.getWantCount()));
        vo.setLocation(buildLocationPath(item.getLocation()));
        vo.setStatus(item.getStatus());
        vo.setCreatedAt(item.getCreatedAt());
        return vo;
    }

    private ItemDetailVO toDetailVO(Item item) {
        ItemListVO base = toListVO(item);
        ItemDetailVO vo = new ItemDetailVO();
        vo.setId(base.getId());
        vo.setTitle(base.getTitle());
        vo.setPrice(base.getPrice());
        vo.setCondition(base.getCondition());
        vo.setCategory(base.getCategory());
        vo.setImage(base.getImage());
        vo.setSellerName(base.getSellerName());
        vo.setSellerAvatar(base.getSellerAvatar());
        vo.setWantCount(base.getWantCount());
        vo.setLocation(base.getLocation());
        vo.setStatus(base.getStatus());
        vo.setCreatedAt(base.getCreatedAt());
        vo.setDescription(item.getDescription());
        vo.setSellerId(item.getSeller().getId());
        vo.setViewCount(nullToZero(item.getViewCount()));
        vo.setImages(item.getImages().stream()
                .sorted(Comparator.comparing(ItemImage::getSortOrder))
                .map(ItemImage::getUrl)
                .toList());
        return vo;
    }

    private String buildSellerName(User seller) {
        if (!isBlank(seller.getNickname())) {
            return seller.getNickname();
        }
        return seller.getStudentId();
    }

    private String buildLocationPath(Location location) {
        if (location == null) {
            return null;
        }

        List<String> names = new ArrayList<>();
        Location current = location;
        while (current != null) {
            names.add(current.getName());
            if (current.getParentId() == null) {
                break;
            }
            current = locationRepository.findById(current.getParentId()).orElse(null);
        }
        Collections.reverse(names);
        return String.join(" / ", names);
    }

    private String normalizeBlank(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }
}
