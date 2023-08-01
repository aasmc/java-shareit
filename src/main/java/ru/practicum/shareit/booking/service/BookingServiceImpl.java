package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.ServiceException;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.model.BookingStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper mapper;
    private final BookingDataValidator bookingDataValidator;
    private final UserBookingsProcessor userBookingsProcessor;

    @Override
    public BookingResponse createBooking(BookingRequest dto) {
        dto.setStatus(WAITING);
        Booking booking = mapper.mapToDomain(dto);
        bookingDataValidator.throwIfItemNotAvailable(dto.getItemId(), booking);
        bookingDataValidator.throwIfBookerIsItemOwner(dto.getBookerId(), booking);
        Booking saved = bookingRepository.save(booking);
        return mapper.mapToDto(saved);
    }

    @Override
    public BookingResponse updateApproved(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = findBookingByIdOrThrow(bookingId);
        bookingDataValidator.throwIfNotOwnerOfBookedItem(ownerId, bookingId, booking);
        if (approved) {
            bookingDataValidator.throwIfBookingAlreadyApproved(ownerId, booking);
            booking.setStatus(APPROVED);
        } else {
            bookingDataValidator.throwIfBookingAlreadyRejected(ownerId, booking);
            booking.setStatus(REJECTED);
        }
        bookingRepository.save(booking);
        return mapper.mapToDto(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public BookingResponse getBookingById(Long bookingId, Long userId) {
        Booking booking = findBookingByIdOrThrow(bookingId);
        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getOwner().getId();
        if (userId.equals(bookerId) || userId.equals(ownerId)) {
            return mapper.mapToDto(booking);
        } else {
            String msg = String.format("Only owner or booker of a Booking can request data about it." +
                    "User with id=%d is neither owner nor booker.", userId);
            throw new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingResponse> getAllBookingsOfUser(Long bookerId, BookingStateDto state, int from, int size) {
        bookingDataValidator.throwIfUserNotExists(bookerId);
        List<Booking> bookings = userBookingsProcessor.getAllBookingsOfUser(false, bookerId, state, from, size);
        return convertResponse(bookings);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingResponse> getAllBookingsOfOwner(Long ownerId, BookingStateDto state, int from, int size) {
        bookingDataValidator.throwIfUserNotExists(ownerId);
        List<Booking> bookings = userBookingsProcessor.getAllBookingsOfUser(true, ownerId, state, from, size);
        return convertResponse(bookings);
    }

    private List<BookingResponse> convertResponse(List<Booking> bookings) {
        return bookings.stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toList());
    }

    private Booking findBookingByIdOrThrow(Long bookingId) {
        return bookingRepository.findBookingByIdItemFetched(bookingId)
                .orElseThrow(() -> {
                    String msg = String.format("Booking with ID=%d not found.", bookingId);
                    return new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
                });
    }
}
