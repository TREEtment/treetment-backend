package com.treetment.backend.EmotionReport;

import java.util.List;

public record OpenAiResponseDTO(List<Choice> choices) {
    public record Choice(MessageDTO message) {
    }
}
