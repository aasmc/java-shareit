package ru.practicum.shareit.gateway.error;

import lombok.Getter;

public class GatewayException extends RuntimeException {
    @Getter
    private final int code;

    public GatewayException(int code, String message) {
        super(message);
        this.code = code;
    }
}
