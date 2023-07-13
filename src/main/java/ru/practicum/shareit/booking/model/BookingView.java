package ru.practicum.shareit.booking.model;


import lombok.Getter;

@Getter
public class BookingView {
    private final Long id;
    private final Long bookerId;

    public BookingView(Long id, Long bookerId) {
        this.id = id;
        this.bookerId = bookerId;
    }
}
