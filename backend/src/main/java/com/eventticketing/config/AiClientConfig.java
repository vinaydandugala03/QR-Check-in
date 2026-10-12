package com.eventticketing.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AiClientConfig {

    @Value("${groq.base-url:https://api.groq.com/openai/v1}")
    private String baseUrl;

    @Value("${groq.model:openai/gpt-oss-120b}")
    private String model;

    @Value("${groq.api-key:}")
    private String apiKey;

    @Value("${groq.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${groq.read-timeout-ms:20000}")
    private int readTimeoutMs;

    @Bean
    public RestClient groqRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        String effectiveApiKey = getApiKey();
        if (effectiveApiKey != null && !effectiveApiKey.isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + effectiveApiKey.trim());
        }

        return builder.build();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getModel() {
        return model;
    }

    public String getApiKey() {
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey;
        }
        String sysProp = System.getProperty("GROQ_API_KEY");
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp;
        }
        return System.getenv("GROQ_API_KEY");
    }

    public boolean isApiKeyConfigured() {
        String key = getApiKey();
        return key != null && !key.isBlank();
    }
}
