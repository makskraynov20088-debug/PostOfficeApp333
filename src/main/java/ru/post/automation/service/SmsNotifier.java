package ru.post.automation.service;

import ru.post.automation.util.AppLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class SmsNotifier implements Notifier {
    private static final Logger LOG = AppLogger.get(SmsNotifier.class);
    private final List<String> sent = new ArrayList<>();

    @Override
    public void send(String phone, String text) {
        sent.add(phone + ": " + text);
        LOG.info("SMS отправлено на " + phone + ": " + text);
    }

    public List<String> getSent() {
        return Collections.unmodifiableList(sent);
    }
}
