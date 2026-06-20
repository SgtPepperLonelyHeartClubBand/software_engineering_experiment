package Market_backend.controller;

import Market_backend.common.BusinessException;
import Market_backend.common.Result;
import Market_backend.common.UserContext;
import Market_backend.dto.ItemListVO;
import Market_backend.dto.OrderVO;
import Market_backend.service.TradeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/items/{id}/reserve")
    public Result<OrderVO> reserve(@PathVariable Long id) {
        return Result.ok(tradeService.reserve(resolveUserId(), id));
    }

    @PostMapping("/orders/{id}/cancel")
    public Result<OrderVO> cancel(@PathVariable Long id) {
        return Result.ok(tradeService.cancel(resolveUserId(), id));
    }

    @PostMapping("/orders/{id}/complete")
    public Result<OrderVO> complete(@PathVariable Long id) {
        return Result.ok(tradeService.complete(resolveUserId(), id));
    }

    @GetMapping("/orders/reserved")
    public Result<List<ItemListVO>> reserved() {
        return Result.ok(tradeService.listReserved(resolveUserId()));
    }

    @GetMapping("/orders/bought")
    public Result<List<ItemListVO>> bought() {
        return Result.ok(tradeService.listBought(resolveUserId()));
    }

    private Long resolveUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }
}
