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
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.model.BookingStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper mapper;
    private final UserRepository userRepository;

    @Override
    public BookingResponse createBooking(BookingRequest dto) {
        dto.setStatus(WAITING);
        Booking booking = mapper.mapToDomain(dto);
        checkItemAvailable(dto, booking);
        checkUserNotOwner(dto, booking);
        Booking saved = bookingRepository.save(booking);
        return mapper.mapToDto(saved);
    }

    private void checkUserNotOwner(BookingRequest dto, Booking booking) {
        if (dto.getBookerId().equals(booking.getItem().getOwner().getId())) {
            String msg = String.format("Cannot book item: {%s} for user with id=%d " +
                    "because the user already owns the item.", booking, dto.getBookerId());
            throw new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
        }
    }

    @Override
    public BookingResponse updateApproved(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = findBookingByIdOrThrow(bookingId);
        checkBookingOwner(ownerId, bookingId, booking);
        if (approved) {
            if (booking.getStatus() == APPROVED) {
                String msg = String.format("Cannot approve already approved Booking: %s " +
                        "by owner with id = %d", booking, ownerId);
                throw new ServiceException(HttpStatus.BAD_REQUEST.value(), msg);
            }
            booking.setStatus(APPROVED);
        } else {
            if (booking.getStatus() == REJECTED) {
                String msg = String.format("Cannot reject already rejected Booking: {%s} " +
                        "by owner with id = %d", booking, ownerId);
                throw new ServiceException(HttpStatus.BAD_REQUEST.value(), msg);
            }
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
    public List<BookingResponse> getAllBookingsOfUser(Long bookerId, BookingStateDto state) {
        checkUserExists(bookerId);
        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByBooker_IdOrderByStartDesc(bookerId);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByBooker_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(bookerId, now, now);
                break;
            case PAST:
                bookings = bookingRepository.findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(bookerId, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBooker_IdAndStartIsAfterOrderByStartDesc(bookerId, now);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByBooker_IdAndStatusEqualsOrderByStartDesc(bookerId, WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBooker_IdAndStatusEqualsOrderByStartDesc(bookerId, REJECTED);
                break;
            default:
                String msg = String.format("Unknown state: %s", state);
                throw new ServiceException(HttpStatus.BAD_REQUEST.value(), msg);
        }
        return convertResponse(bookings);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingResponse> getAllBookingsOfOwner(Long ownerId, BookingStateDto state) {
        checkUserExists(ownerId);
        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByItem_Owner_IdOrderByStartDesc(ownerId);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(ownerId, now, now);
                break;
            case PAST:
                bookings = bookingRepository.findAllByItem_Owner_IdAndEndIsBeforeOrderByStartDesc(ownerId, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByItem_Owner_IdAndStartIsAfterOrderByStartDesc(ownerId, now);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByItem_Owner_IdAndStatusEqualsOrderByStartDesc(ownerId, WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByItem_Owner_IdAndStatusEqualsOrderByStartDesc(ownerId, REJECTED);
                break;
            default:
                String msg = String.format("Unknown state: %s", state);
                throw new ServiceException(HttpStatus.BAD_REQUEST.value(), msg);
        }
        return convertResponse(bookings);
    }

    private List<BookingResponse> convertResponse(List<Booking> bookings) {
        return bookings.stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toList());
    }

    private Booking findBookingByIdOrThrow(Long bookingId) {
        return bookingRepository.findBookingById(bookingId)
                .orElseThrow(() -> {
                    String msg = String.format("Booking with ID=%d not found.", bookingId);
                    return new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
                });
    }

    private void checkBookingOwner(Long ownerId, Long bookingId, Booking booking) {
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            String msg = String.format("Status of booking with ID=%d cannot be updated because " +
                    "user with ID=%d is not the owner of item.", bookingId, ownerId);
            throw new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
        }
    }

    private void checkItemAvailable(BookingRequest dto, Booking booking) {
        if (!booking.getItem().getAvailable()) {
            String msg = String.format("Cannot book item with ID=%d because it is not available", dto.getItemId());
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), msg);
        }
    }

    private void checkUserExists(Long bookerId) {
        if (!userRepository.existsById(bookerId)) {
            String msg = String.format("User with ID=%d not found.", bookerId);
            throw new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
        }
    }
}
