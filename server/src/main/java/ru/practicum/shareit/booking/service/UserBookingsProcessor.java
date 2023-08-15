package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface UserBookingsProcessor {
    List<Booking> getAllBookingsOfUser(boolean isOwner,
                                       Long userId,
                                       BookingStateDto state,
                                       int from,
                                       int size);

}
