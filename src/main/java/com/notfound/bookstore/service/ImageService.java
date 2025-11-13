package com.notfound.bookstore.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ImageService {
    Map<String, Object> uploadImage(MultipartFile file);

    List<Map<String, Object>> uploadMultipleImages(List<MultipartFile> files);

    boolean deleteImage(String publicId);
}
