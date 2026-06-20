package Market_backend.controller;

import Market_backend.common.BusinessException;
import Market_backend.common.Result;
import Market_backend.common.UserContext;
import Market_backend.dto.ItemDetailVO;
import Market_backend.dto.ItemListVO;
import Market_backend.dto.ItemRequest;
import Market_backend.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public Result<List<ItemListVO>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        return Result.ok(itemService.listItems(category, keyword, status));
    }

    @GetMapping("/mine")
    public Result<List<ItemListVO>> mine() {
        return Result.ok(itemService.listMyItems(resolveUserId()));
    }

    @GetMapping("/{id}")
    public Result<ItemDetailVO> detail(@PathVariable Long id) {
        return Result.ok(itemService.getItemDetail(id, resolveUserId()));
    }

    @PostMapping
    public Result<ItemDetailVO> create(@Valid @RequestBody ItemRequest request) {
        return Result.ok(itemService.createItem(resolveUserId(), request));
    }

    @PutMapping("/{id}")
    public Result<ItemDetailVO> update(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return Result.ok(itemService.updateItem(resolveUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        itemService.deleteItem(resolveUserId(), id);
        return Result.ok();
    }

    private Long resolveUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }
}
