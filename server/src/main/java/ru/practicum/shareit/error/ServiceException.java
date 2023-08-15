package ru.practicum.shareit.error;

import lombok.Getter;

public class ServiceException extends RuntimeException {
    @Getter
    private final int code;

    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }
}
