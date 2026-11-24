package ru.post.automation.service;

public interface Notifier {
    void send(String phone, String text);
}
