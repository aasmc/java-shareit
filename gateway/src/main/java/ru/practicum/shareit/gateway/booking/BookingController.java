package ru.practicum.shareit.gateway.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.gateway.booking.dto.BookingState;
import ru.practicum.shareit.gateway.error.GatewayException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> bookItem(@RequestHeader(USER_HEADER) long userId,
                                           @RequestBody @Valid BookItemRequestDto requestDto) {
        log.info("Creating booking {}, userId={}", requestDto, userId);
        return bookingClient.bookItem(userId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateStatus(@PathVariable("bookingId") Long bookingId,
                                               @RequestParam("approved") boolean approved,
                                               @RequestHeader(USER_HEADER) long userId) {
        log.info(
                "Received PATCH request to update approval status of Booking with ID={} to status={}",
                bookingId,
                approved
        );
        return bookingClient.updateBooking(bookingId, approved, userId);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader(USER_HEADER) long userId,
                                             @PathVariable Long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookings(@RequestHeader(USER_HEADER) long userId,
                                              @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new GatewayException(HttpStatus.BAD_REQUEST.value(), "Unknown state: " + stateParam));
        log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsOfOwner(@RequestHeader(USER_HEADER) long ownerId,
                                                     @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                                     @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                     @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new GatewayException(HttpStatus.BAD_REQUEST.value(), "Unknown state: " + stateParam));
        log.info(
                "Received request to GET all bookings of owner with id={}, in state={}",
                ownerId,
                state
        );
        return bookingClient.getBookingsOfOwner(ownerId, state, from, size);
    }

}
