package com.eventticketing.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatResponse {

    private String reply;
    private String conversationId;
    private List<Long> referencedEventIds = new ArrayList<>();
    private LocalDateTime timestamp;

    public ChatResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatResponse(String reply) {
        this.reply = reply;
        this.timestamp = LocalDateTime.now();
    }

    public ChatResponse(String reply, String conversationId, List<Long> referencedEventIds) {
        this.reply = reply;
        this.conversationId = conversationId;
        this.referencedEventIds = referencedEventIds != null ? referencedEventIds : new ArrayList<>();
        this.timestamp = LocalDateTime.now();
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public List<Long> getReferencedEventIds() {
        return referencedEventIds;
    }

    public void setReferencedEventIds(List<Long> referencedEventIds) {
        this.referencedEventIds = referencedEventIds != null ? referencedEventIds : new ArrayList<>();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
