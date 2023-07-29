package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@RequestBody @Valid BookingRequest dto,
                                         @RequestHeader(USER_HEADER) Long userId) {
        log.info("Received POST request to create Booking={}", dto);
        dto.setBookerId(userId);
        return bookingService.createBooking(dto);
    }

    @PatchMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public BookingResponse updateStatus(@PathVariable("bookingId") Long bookingId,
                                        @RequestParam("approved") boolean approved,
                                        @RequestHeader(USER_HEADER) Long userId) {
        log.info(
                "Received PATCH request to update approval status of Booking with ID={} to status={}",
                bookingId,
                approved
        );
        return bookingService.updateApproved(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public BookingResponse getBookingById(@PathVariable("bookingId") Long bookingId,
                                          @RequestHeader(USER_HEADER) Long userId) {
        log.info(
                "Received request to GET booking with id={} by user/owner with id={}",
                bookingId,
                userId
        );
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<BookingResponse> getBookingsOfUser(@RequestHeader(USER_HEADER) Long bookerId,
                                                   @RequestParam(value = "state", required = false, defaultValue = "ALL") String state,
                                                   @RequestParam(value = "from", defaultValue = "0", required = false) @PositiveOrZero int from,
                                                   @RequestParam(value = "size", defaultValue = "10", required = false) @PositiveOrZero int size) {
        log.info(
                "Received request to GET all bookings of user with id={} in state={}",
                bookerId,
                state
        );
        return bookingService.getAllBookingsOfUser(bookerId, BookingStateDto.fromString(state), from, size);
    }

    @GetMapping("/owner")
    @ResponseStatus(HttpStatus.OK)
    public List<BookingResponse> getBookingsOfOwner(@RequestHeader(USER_HEADER) Long ownerId,
                                                    @RequestParam(value = "state", required = false, defaultValue = "ALL") String state,
                                                    @RequestParam(value = "from", defaultValue = "0", required = false) @PositiveOrZero int from,
                                                    @RequestParam(value = "size", defaultValue = "10", required = false) @PositiveOrZero int size) {
        log.info(
                "Received request to GET all bookings of owner with id={}, in state={}",
                ownerId,
                state
        );
        return bookingService.getAllBookingsOfOwner(ownerId, BookingStateDto.fromString(state), from, size);
    }

}
