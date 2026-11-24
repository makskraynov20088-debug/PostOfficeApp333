package ru.post.automation.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.post.automation.model.Shipment;
import ru.post.automation.model.ShipmentStatus;
import ru.post.automation.service.NoFreeCellException;
import ru.post.automation.service.RegistrationService;
import ru.post.automation.service.ShipmentNotFoundException;
import ru.post.automation.service.TrackingService;
import ru.post.automation.util.AppLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Logger;

public class ApiServer {
    private static final Logger LOG = AppLogger.get(ApiServer.class);
    private static final String BASE = "/api/shipments";
    private final HttpServer server;
    private final RegistrationService registration;
    private final TrackingService tracking;

    public ApiServer(int port, RegistrationService registration, TrackingService tracking) throws IOException {
        this.registration = registration;
        this.tracking = tracking;
        this.server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        this.server.createContext(BASE, this::handle);
    }

    public void start() {
        server.start();
        LOG.info("API запущен на порту " + getPort());
    }

    public void stop() {
        server.stop(0);
        LOG.info("API остановлен");
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    private void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();
        try {
            if (path.equals(BASE) && method.equals("POST")) {
                register(ex);
            } else if (path.matches(BASE + "/\\d+") && method.equals("GET")) {
                Shipment s = tracking.find(path.substring(BASE.length() + 1));
                send(ex, 200, Json.shipment(s));
            } else if (path.matches(BASE + "/\\d+/status") && method.equals("POST")) {
                changeStatus(ex, path.split("/")[3]);
            } else {
                send(ex, 405, Json.error("Метод или адрес не поддерживается"));
            }
        } catch (ShipmentNotFoundException e) {
            send(ex, 404, Json.error(e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException | NoFreeCellException e) {
            send(ex, 400, Json.error(e.getMessage()));
        } catch (RuntimeException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Ошибка обработки запроса " + method + " " + path, e);
            send(ex, 500, Json.error("Внутренняя ошибка сервера"));
        }
    }

    private void register(HttpExchange ex) throws IOException {
        Map<String, String> data = Json.parse(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        double weight = Double.parseDouble(data.getOrDefault("weightKg", "0"));
        int zone = Integer.parseInt(data.getOrDefault("zone", "0"));
        Shipment s = registration.register(data.get("sender"), data.get("recipient"),
                data.get("phone"), weight, zone);
        send(ex, 201, Json.shipment(s));
    }

    private void changeStatus(HttpExchange ex, String track) throws IOException {
        Map<String, String> data = Json.parse(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        ShipmentStatus next = ShipmentStatus.valueOf(data.getOrDefault("status", ""));
        send(ex, 200, Json.shipment(registration.moveStatus(track, next)));
    }

    private void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
