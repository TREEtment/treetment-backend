package com.treetment.backend.emotionRecord.dto;
import java.util.List;
public record OpenAiRequestDTO(String model, List<MessageDTO> messages) {
}
