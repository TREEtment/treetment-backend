package com.treetment.backend.file;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    
    @Value("${cloud.aws.region.static:ap-northeast-2}")
    private String region;

    public String uploadImage(MultipartFile file, Long userId) throws IOException {
        return upload(file, "images", userId);
    }

    public String uploadFile(MultipartFile file) throws IOException {
        return upload(file, "files", null);
    }

    private String upload(MultipartFile file, String folder, Long userId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : "";

        String key;
        if (userId != null) {
            key = String.format("%s/%d/%s/%s%s",
                    folder,
                    userId,
                    LocalDate.now(),
                    UUID.randomUUID(),
                    extension);
        } else {
            key = String.format("%s/%s/%s%s",
                    folder,
                    LocalDate.now(),
                    UUID.randomUUID(),
                    extension);
        }

        String contentType = file.getContentType();
        if (contentType == null && originalFilename != null) {
            contentType = URLConnection.guessContentTypeFromName(originalFilename);
        }

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        try {
            s3Client.putObject(putReq, RequestBody.fromBytes(file.getBytes()));
        } catch (S3Exception e) {
            throw new IOException("S3 업로드 실패: " + e.awsErrorDetails().errorMessage(), e);
        }

        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }
}


