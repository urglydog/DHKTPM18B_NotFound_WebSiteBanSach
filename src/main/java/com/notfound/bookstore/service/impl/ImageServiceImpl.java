package com.notfound.bookstore.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.notfound.bookstore.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final Cloudinary cloudinary;

    @Override
    public Map<String, Object> uploadImage(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File is empty or null");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "bookstore/books",
                            "resource_type", "image",
                            "overwrite", true,
                            "invalidate", true
                    )
            );

            log.info("Image uploaded successfully: {}", uploadResult.get("url"));
            return uploadResult;

        } catch (IOException e) {
            log.error("Error uploading image: {}", e.getMessage());
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> uploadMultipleImages(List<MultipartFile> files) {
        List<Map<String, Object>> uploadResults = new ArrayList<>();

        if (files == null || files.isEmpty()) {
            return uploadResults;
        }

        for (MultipartFile file : files) {
            try {
                Map<String, Object> result = uploadImage(file);
                uploadResults.add(result);
            } catch (Exception e) {
                log.error("Error uploading image {}: {}", file.getOriginalFilename(), e.getMessage());
            }
        }

        return uploadResults;
    }

    @Override
    public boolean deleteImage(String publicId) {
        try {
            if (publicId == null || publicId.isEmpty()) {
                return false;
            }

            if (publicId.contains("/")) {
                String[] parts = publicId.split("/");
                publicId = parts[parts.length - 1].replaceAll("\\.[^.]+$", "");
                publicId = "bookstore/books/" + publicId;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String resultStatus = (String) result.get("result");

            log.info("Image deletion result: {} for publicId: {}", resultStatus, publicId);
            return "ok".equals(resultStatus);

        } catch (Exception e) {
            log.error("Error deleting image with publicId {}: {}", publicId, e.getMessage());
            return false;
        }
    }
}
