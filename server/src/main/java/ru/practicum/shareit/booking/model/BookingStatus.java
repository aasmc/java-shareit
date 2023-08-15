package ru.practicum.shareit.booking.model;

public enum BookingStatus {
    /**
     * New Booking pending approval.
     */
    WAITING,
    /**
     * Booking approved by owner of Item.
     */
    APPROVED,
    /**
     * Booking rejected by owner of Item.
     */
    REJECTED,
    /**
     * Booking cancelled by booker.
     */
    CANCELED
}
