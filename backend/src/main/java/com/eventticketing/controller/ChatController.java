package com.eventticketing.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventticketing.dto.ChatRequest;
import com.eventticketing.dto.ChatResponse;
import com.eventticketing.exception.RateLimitExceededException;
import com.eventticketing.security.UserPrincipal;
import com.eventticketing.service.ChatService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final int MAX_REQUESTS_PER_WINDOW = 20;
    private static final long WINDOW_DURATION_MS = 60_000L; // 1 minute
    private static final int MAX_TRACKED_IPS = 1000;

    private final ChatService chatService;
    private final Map<String, RequestCounter> rateLimitTracker = new ConcurrentHashMap<>();

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {

        checkRateLimit(httpRequest);
        ChatResponse response = chatService.processChat(request, principal);
        return ResponseEntity.ok(response);
    }

    private void checkRateLimit(HttpServletRequest request) {
        String clientIp = resolveClientIp(request);
        long now = System.currentTimeMillis();

        if (rateLimitTracker.size() > MAX_TRACKED_IPS) {
            rateLimitTracker.entrySet().removeIf(entry -> now - entry.getValue().windowStart > WINDOW_DURATION_MS);
        }

        rateLimitTracker.compute(clientIp, (key, existing) -> {
            if (existing == null || (now - existing.windowStart) > WINDOW_DURATION_MS) {
                return new RequestCounter(now, 1);
            }
            if (existing.count >= MAX_REQUESTS_PER_WINDOW) {
                throw new RateLimitExceededException("Too many chat requests. Please wait a moment before trying again.");
            }
            existing.count++;
            return existing;
        });
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown-client";
    }

    private static class RequestCounter {
        private final long windowStart;
        private int count;

        public RequestCounter(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}

