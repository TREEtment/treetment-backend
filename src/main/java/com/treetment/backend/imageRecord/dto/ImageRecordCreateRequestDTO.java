package com.treetment.backend.imageRecord.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageRecordCreateRequestDTO {
    private String emotionImage; // 클라이언트가 업로드 후 전달하는 이미지 URL
}
