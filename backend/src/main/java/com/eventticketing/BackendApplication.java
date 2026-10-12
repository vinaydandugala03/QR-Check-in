package com.eventticketing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(BackendApplication.class, args);
    }

    /**
     * Normalizes a database URL to ensure it starts with jdbc: for MySQL Connector/J.
     */
    public static String normalizeJdbcUrl(String url) {
        if (url == null) {
            return null;
        }
        String normalized = url.trim();
        if (normalized.isEmpty()) {
            return "";
        }
        if (normalized.startsWith("mysql://")) {
            normalized = "jdbc:" + normalized;
        }
        if (normalized.contains("ssl-mode=")) {
            normalized = normalized.replace("ssl-mode=", "sslMode=");
        }
        return normalized;
    }

    /**
     * Loads local .env configuration files into Java System properties
     * if they exist and are not already set in the OS environment.
     */
    public static void loadDotenv() {
        String userDir = System.getProperty("user.dir", ".");
        File[] candidateFiles = {
            new File(userDir, ".env"),
            new File(userDir, "backend/.env"),
            new File(".env"),
            new File("backend/.env"),
            new File("../.env"),
            new File("../backend/.env")
        };

        for (File file : candidateFiles) {
            if (file.exists() && file.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                            continue;
                        }
                        int eqIdx = line.indexOf('=');
                        String key = line.substring(0, eqIdx).trim();
                        String value = line.substring(eqIdx + 1).trim();

                        if ((value.startsWith("\"") && value.endsWith("\""))
                                || (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }

                        // Automatically normalize database URLs missing the jdbc: prefix
                        if ("DB_URL".equalsIgnoreCase(key)) {
                            value = normalizeJdbcUrl(value);
                        }

                        // Only set if not already defined in OS environment or system properties
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                    break;
                } catch (Exception ignored) {
                    // Fall back to application.properties defaults if unreadable
                }
            }
        }
    }
}
