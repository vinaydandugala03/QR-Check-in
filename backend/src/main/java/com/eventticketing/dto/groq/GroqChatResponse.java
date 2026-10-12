package com.eventticketing.dto.groq;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GroqChatResponse(
        String id,
        List<Choice> choices
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(
            int index,
            GroqMessage message,
            @JsonProperty("finish_reason")
            String finishReason
    ) {
    }
}

