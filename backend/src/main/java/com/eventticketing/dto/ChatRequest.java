package com.eventticketing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatRequest {

    @NotBlank(message = "Message cannot be blank")
    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;

    private String conversationId;

    public ChatRequest() {
    }

    public ChatRequest(String message) {
        this.message = message;
    }

    public ChatRequest(String message, String conversationId) {
        this.message = message;
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
