package ru.post.automation.api;

import ru.post.automation.model.Shipment;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Json {
    private static final Pattern PAIR = Pattern.compile("\"(\\w+)\"\\s*:\\s*(\"([^\"]*)\"|[-0-9.]+)");

    private Json() {
    }

    public static Map<String, String> parse(String body) {
        Map<String, String> result = new HashMap<>();
        Matcher m = PAIR.matcher(body == null ? "" : body);
        while (m.find()) {
            result.put(m.group(1), m.group(3) != null ? m.group(3) : m.group(2));
        }
        return result;
    }

    public static String error(String message) {
        return "{\"error\":\"" + escape(message) + "\"}";
    }

    public static String shipment(Shipment s) {
        return "{\"trackNumber\":\"" + s.getTrackNumber() + "\","
                + "\"sender\":\"" + escape(s.getSenderName()) + "\","
                + "\"recipient\":\"" + escape(s.getRecipientName()) + "\","
                + "\"weightKg\":" + s.getWeightKg() + ","
                + "\"zone\":" + s.getZone() + ","
                + "\"cost\":" + s.getCost() + ","
                + "\"cell\":\"" + s.getCellCode() + "\","
                + "\"status\":\"" + s.getStatus().name() + "\"}";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
