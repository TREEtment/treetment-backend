package com.treetment.backend.file;

import com.treetment.backend.global.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final S3StorageService s3StorageService;

    @PostMapping("/image")
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        String url = s3StorageService.uploadImage(file);
        return ResponseEntity.ok(ApiResponse.success("이미지 업로드 완료", url));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        String url = s3StorageService.uploadFile(file);
        return ResponseEntity.ok(ApiResponse.success("파일 업로드 완료", url));
    }
}


