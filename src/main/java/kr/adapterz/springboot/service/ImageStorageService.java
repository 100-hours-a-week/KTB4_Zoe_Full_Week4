package kr.adapterz.springboot.service;

import kr.adapterz.springboot.exception.ImageUploadFailedException;
import kr.adapterz.springboot.exception.InvalidImageFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".webp",
            ".gif"
    );

    private final String postImageDir;
    private final String profileImageDir;
    private final String postImageUrlPrefix;
    private final String profileImageUrlPrefix;

    public ImageStorageService(
            @Value("${app.upload.post-image-dir:uploads/post-images}") String postImageDir,
            @Value("${app.upload.profile-image-dir:uploads/profile-images}") String profileImageDir,
            @Value("${app.upload.post-image-url-prefix:/uploads/post-images}") String postImageUrlPrefix,
            @Value("${app.upload.profile-image-url-prefix:/uploads/profile-images}") String profileImageUrlPrefix
    ) {
        this.postImageDir = postImageDir;
        this.profileImageDir = profileImageDir;
        this.postImageUrlPrefix = postImageUrlPrefix;
        this.profileImageUrlPrefix = profileImageUrlPrefix;
    }

    public List<String> storePostImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        return images.stream()
                .filter(image -> image != null && !image.isEmpty())
                .map(image -> store(image, postImageDir, postImageUrlPrefix))
                .toList();
    }

    public String storeProfileImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }

        return store(image, profileImageDir, profileImageUrlPrefix);
    }

    private String store(MultipartFile image, String imageDir, String imageUrlPrefix) {
        validateImage(image);

        try {
            Path uploadDir = Path.of(imageDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            String extension = getExtension(image.getOriginalFilename());
            String storedFileName = UUID.randomUUID() + extension;
            Path targetPath = uploadDir.resolve(storedFileName).normalize();

            if (!targetPath.startsWith(uploadDir)) {
                throw new InvalidImageFileException();
            }

            image.transferTo(targetPath);
            return imageUrlPrefix + "/" + storedFileName;
        } catch (IOException e) {
            throw new ImageUploadFailedException(e);
        }
    }

    private void validateImage(MultipartFile image) {
        String contentType = image.getContentType();

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidImageFileException();
        }

        String extension = getExtension(image.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidImageFileException();
        }
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidImageFileException();
        }

        int extensionStartIndex = originalFilename.lastIndexOf(".");
        if (extensionStartIndex < 0) {
            throw new InvalidImageFileException();
        }

        return originalFilename.substring(extensionStartIndex).toLowerCase();
    }
}
