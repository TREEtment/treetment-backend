package com.treetment.backend.TextModel;
import java.util.List;
public record OpenAiRequestDTO(String model, List<MessageDTO> messages) {
}
