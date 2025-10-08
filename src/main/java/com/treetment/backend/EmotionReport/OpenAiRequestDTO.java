package com.treetment.backend.EmotionReport;

import java.util.List;

public record OpenAiRequestDTO(String model, List<MessageDTO> messages) {
}
