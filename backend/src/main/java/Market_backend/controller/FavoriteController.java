package Market_backend.controller;

import Market_backend.common.BusinessException;
import Market_backend.common.Result;
import Market_backend.common.UserContext;
import Market_backend.dto.ItemListVO;
import Market_backend.service.FavoriteService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/items/{id}/favorite")
    public Result<Void> favorite(@PathVariable Long id) {
        favoriteService.favorite(resolveUserId(), id);
        return Result.ok();
    }

    @DeleteMapping("/items/{id}/favorite")
    public Result<Void> unfavorite(@PathVariable Long id) {
        favoriteService.unfavorite(resolveUserId(), id);
        return Result.ok();
    }

    @GetMapping("/favorites")
    public Result<List<ItemListVO>> favorites() {
        return Result.ok(favoriteService.listFavorites(resolveUserId()));
    }

    private Long resolveUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }
}
