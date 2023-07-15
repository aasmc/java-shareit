package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.user.repository.UserRepository;

import static ru.practicum.shareit.booking.model.BookingStatus.APPROVED;
import static ru.practicum.shareit.booking.model.BookingStatus.REJECTED;

@Service
@RequiredArgsConstructor
public class BookingDataValidatorImpl implements BookingDataValidator {

    private final UserRepository userRepository;

    @Override
    public void throwIfNotOwnerOfBooking(Long ownerId, Long bookingId, Booking booking) {
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            String msg = String.format("Status of booking with ID=%d cannot be updated because " +
                    "user with ID=%d is not the owner of item.", bookingId, ownerId);
            throw new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
        }
    }

    @Override
    public void throwIfItemNotAvailable(Long itemId, Booking booking) {
        if (!booking.getItem().getAvailable()) {
            String msg = String.format("Cannot book item with ID=%d because it is not available", itemId);
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), msg);
        }
    }

    @Override
    public void throwIfUserNotExists(Long bookerId) {
        if (!userRepository.existsById(bookerId)) {
            String msg = String.format("User with ID=%d not found.", bookerId);
            throw new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
        }
    }

    @Override
    public void throwIfBookerIsItemOwner(Long bookerId, Booking booking) {
        if (bookerId.equals(booking.getItem().getOwner().getId())) {
            String msg = String.format("Cannot book item: {%s} for user with id=%d " +
                    "because the user already owns the item.", booking, bookerId);
            throw new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
        }
    }

    @Override
    public void throwIfBookingAlreadyRejected(Long ownerId, Booking booking) {
        if (booking.getStatus() == REJECTED) {
            String msg = String.format("Cannot reject already rejected Booking: {%s} " +
                    "by owner with id = %d", booking, ownerId);
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), msg);
        }
    }

    @Override
    public void throwIfBookingAlreadyApproved(Long ownerId, Booking booking) {
        if (booking.getStatus() == APPROVED) {
            String msg = String.format("Cannot approve already approved Booking: %s " +
                    "by owner with id = %d", booking, ownerId);
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), msg);
        }
    }
}
