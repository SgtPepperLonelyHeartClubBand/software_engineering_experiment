package Market_backend.controller;

import Market_backend.common.Result;
import Market_backend.dto.LocationNodeVO;
import Market_backend.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final UserService userService;

    public LocationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/tree")
    public Result<List<LocationNodeVO>> tree() {
        return Result.ok(userService.getLocationTree());
    }
}
