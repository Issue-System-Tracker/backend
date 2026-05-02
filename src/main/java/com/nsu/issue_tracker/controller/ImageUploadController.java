package com.nsu.issue_tracker.controller;

import com.nsu.issue_tracker.config.FileUploadProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class ImageUploadController {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private final FileUploadProperties uploadProperties;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file)
            throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            return ResponseEntity.badRequest().build();
        }

        String extension = extensionForContentType(contentType);
        String filename = UUID.randomUUID() + extension;

        Path dir = Paths.get(uploadProperties.getDir()).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Path target = dir.resolve(filename);
        file.transferTo(target.toFile());

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(filename)
                .build()
                .toUriString();

        return ResponseEntity.ok(Map.of("url", url));
    }

    private static String extensionForContentType(String contentType) {
        String lower = contentType.toLowerCase(Locale.ROOT);
        if (lower.contains("jpeg")) return ".jpg";
        if (lower.contains("png")) return ".png";
        if (lower.contains("gif")) return ".gif";
        if (lower.contains("webp")) return ".webp";
        return ".img";
    }
}
