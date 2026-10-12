package com.eventticketing.dto.groq;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GroqChatRequest(
        String model,
        List<GroqMessage> messages,
        Double temperature,
        @JsonProperty("max_tokens")
        Integer maxTokens
) {
}

