package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.Booking;

public interface BookingDataValidator {

    void throwIfNotOwnerOfBooking(Long ownerId, Long bookingId, Booking booking);

    void throwIfItemNotAvailable(Long itemId, Booking booking);

    void throwIfUserNotExists(Long userId);

    void throwIfBookerIsItemOwner(Long userId, Booking booking);

    void throwIfBookingAlreadyRejected(Long ownerId, Booking booking);

    void throwIfBookingAlreadyApproved(Long ownerId, Booking booking);

}
