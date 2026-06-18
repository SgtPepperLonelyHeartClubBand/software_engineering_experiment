package Market_backend.service;

import Market_backend.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path uploadRoot;
    private final long maxSizeBytes;

    public UploadService(
            @Value("${app.upload.dir:uploads}") String uploadDir,
            @Value("${app.upload.max-size-bytes:5242880}") long maxSizeBytes
    ) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxSizeBytes = maxSizeBytes;
    }

    public String uploadImage(MultipartFile file) {
        validateImage(file);

        String extension = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + extension;
        Path target = uploadRoot.resolve(filename).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new BusinessException(400, "文件名不合法");
        }

        try {
            Files.createDirectories(uploadRoot);
            file.transferTo(target);
        } catch (IOException ex) {
            throw new BusinessException(500, "图片保存失败");
        }
        return "/uploads/" + filename;
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的图片");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new BusinessException(400, "图片不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BusinessException(400, "仅支持图片文件");
        }
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(400, "仅支持 jpg、png、gif、webp 图片");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new BusinessException(400, "文件名不能为空");
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            throw new BusinessException(400, "图片格式不支持");
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
