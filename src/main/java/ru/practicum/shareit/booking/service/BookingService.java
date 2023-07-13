package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.dto.BookingStateDto;

import java.util.List;

public interface BookingService {

    BookingResponse createBooking(BookingRequest dto);

    BookingResponse updateApproved(Long ownerId, Long bookingId, boolean approved);

    BookingResponse getBookingById(Long bookingId, Long userId);

    List<BookingResponse> getAllBookingsOfUser(Long bookerId, BookingStateDto state);

    List<BookingResponse> getAllBookingsOfOwner(Long ownerId, BookingStateDto state);
}
