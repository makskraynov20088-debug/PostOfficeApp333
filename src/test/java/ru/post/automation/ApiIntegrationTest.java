package ru.post.automation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.post.automation.api.ApiClient;
import ru.post.automation.api.ApiServer;
import ru.post.automation.api.Json;
import ru.post.automation.service.RegistrationService;
import ru.post.automation.service.SmsNotifier;
import ru.post.automation.service.TariffCalculator;
import ru.post.automation.service.TrackingService;
import ru.post.automation.service.WarehouseService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiIntegrationTest {
    private ApiServer server;
    private ApiClient client;

    @BeforeEach
    void setUp() throws IOException {
        TrackingService tracking = new TrackingService();
        RegistrationService registration = new RegistrationService(
                new TariffCalculator(), new WarehouseService(5), tracking, new SmsNotifier());
        server = new ApiServer(0, registration, tracking);
        server.start();
        client = new ApiClient("http://localhost:" + server.getPort());
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    private static final String VALID = "{\"sender\":\"Крайнов\",\"recipient\":\"Попов\","
            + "\"phone\":\"+79052223344\",\"weightKg\":2.2,\"zone\":2}";

    @Test
    void postCreatesShipment() {
        ApiClient.Reply reply = client.post("/api/shipments", VALID);
        assertEquals(201, reply.status());
        assertNotNull(Json.parse(reply.body()).get("trackNumber"));
        assertTrue(reply.body().contains("320.0"));
    }

    @Test
    void getReturnsCreatedShipment() {
        String track = Json.parse(client.post("/api/shipments", VALID).body()).get("trackNumber");
        ApiClient.Reply reply = client.get(track);
        assertEquals(200, reply.status());
        assertTrue(reply.body().contains("ACCEPTED"));
    }

    @Test
    void getUnknownReturns404() {
        assertEquals(404, client.get("00000000000000").status());
    }

    @Test
    void invalidWeightReturns400() {
        String body = VALID.replace("2.2", "0");
        assertEquals(400, client.post("/api/shipments", body).status());
    }

    @Test
    void statusChangeThroughApi() {
        String track = Json.parse(client.post("/api/shipments", VALID).body()).get("trackNumber");
        ApiClient.Reply reply = client.post("/api/shipments/" + track + "/status", "{\"status\":\"IN_TRANSIT\"}");
        assertEquals(200, reply.status());
        assertTrue(reply.body().contains("IN_TRANSIT"));
    }
}
