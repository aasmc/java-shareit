package ru.practicum.shareit.booking.dto;

public enum BookingStateDto {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static BookingStateDto fromString(String str) {
        return BookingStateDto.valueOf(str);
    }
}
