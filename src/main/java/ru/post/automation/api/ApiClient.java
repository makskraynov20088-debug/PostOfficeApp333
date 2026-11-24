package ru.post.automation.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ApiClient {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Reply get(String track) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/shipments/" + track)).GET().build();
        return execute(request);
    }

    public Reply post(String path, String json) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        return execute(request);
    }

    private Reply execute(HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new Reply(response.statusCode(), response.body());
        } catch (IOException e) {
            throw new IllegalStateException("Сервис отслеживания недоступен: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Запрос прерван", e);
        }
    }

    public record Reply(int status, String body) {
    }
}
