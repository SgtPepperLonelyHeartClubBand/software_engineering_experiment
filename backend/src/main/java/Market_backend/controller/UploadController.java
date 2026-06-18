package Market_backend.controller;

import Market_backend.common.Result;
import Market_backend.dto.UploadImageVO;
import Market_backend.service.UploadService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/image")
    public Result<UploadImageVO> uploadImage(@RequestParam("file") MultipartFile file) {
        return Result.ok(new UploadImageVO(uploadService.uploadImage(file)));
    }
}
