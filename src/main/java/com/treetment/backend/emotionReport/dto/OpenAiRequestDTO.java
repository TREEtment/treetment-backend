package com.treetment.backend.emotionReport.dto;

import java.util.List;

public record OpenAiRequestDTO(String model, List<MessageDTO> messages) {
}
