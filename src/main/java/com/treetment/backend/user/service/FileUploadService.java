package com.treetment.backend.user.service;

import com.treetment.backend.file.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final S3StorageService s3StorageService;

    public String uploadImage(MultipartFile imageFile, Long userId) {
        try {
            return s3StorageService.uploadImage(imageFile, userId);
        } catch (Exception e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }

    public String uploadFile(MultipartFile file) {
        try {
            return s3StorageService.uploadFile(file);
        } catch (Exception e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }
}
