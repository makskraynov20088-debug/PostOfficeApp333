package ru.post.automation.service;

public class NoFreeCellException extends RuntimeException {
    public NoFreeCellException(String message) {
        super(message);
    }
}
