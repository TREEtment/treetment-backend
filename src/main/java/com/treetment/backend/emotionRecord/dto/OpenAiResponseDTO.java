package com.treetment.backend.emotionRecord.dto;

import java.util.List;

public record OpenAiResponseDTO(List<Choice> choices) {
    public record Choice(MessageDTO message) {
    }
}
