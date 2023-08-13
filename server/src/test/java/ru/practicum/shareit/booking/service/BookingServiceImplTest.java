package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.ServiceException;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.testutil.TestConstants.*;
import static ru.practicum.shareit.testutil.TestDataProvider.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingMapper mapper;
    @Mock
    private BookingDataValidator bookingDataValidator;
    @Mock
    private UserBookingsProcessor userBookingsProcessor;
    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void getBookingById_whenUserIsBooker_returnsBooking() {
        Booking booking = getBooking();
        Mockito
                .when(bookingRepository.findBookingByIdItemFetched(BOOKING_ID))
                .thenReturn(Optional.of(booking));
        BookingResponse expected = fromBooking(booking);
        Mockito
                .when(mapper.mapToDto(booking)).thenReturn(expected);

        BookingResponse actual = bookingService.getBookingById(BOOKING_ID, BOOKER_ID);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getBookingById_whenUserIsOwner_returnsBooking() {
        Booking booking = getBooking();
        Mockito
                .when(bookingRepository.findBookingByIdItemFetched(BOOKING_ID))
                .thenReturn(Optional.of(booking));
        BookingResponse expected = fromBooking(booking);
        Mockito
                .when(mapper.mapToDto(booking)).thenReturn(expected);

        BookingResponse actual = bookingService.getBookingById(BOOKING_ID, OWNER_ID);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getBookingById_whenUserIsNeitherBookerNorOwner_throws() {
        Booking booking = getBooking();
        Mockito
                .when(bookingRepository.findBookingByIdItemFetched(BOOKING_ID))
                .thenReturn(Optional.of(booking));

        assertThrows(ServiceException.class,
                () -> bookingService.getBookingById(BOOKING_ID, OWNER_ID + 100));
    }

    @Test
    void updateApproved_whenReject_andBookingNotRejected_returnsRejectedBooking() {
        BookingRequest dto = getBookingRequest(ITEM_ID, BOOKER_ID);
        dto.setStatus(BookingStatus.WAITING);
        Booking booking = fromBookingRequest(dto);
        BookingResponse expected = fromBooking(booking);
        expected.setStatus(BookingStatus.REJECTED);

        Mockito
                .when(bookingRepository.findBookingByIdItemFetched(dto.getId()))
                .thenReturn(Optional.of(booking));
        Mockito
                .when(bookingRepository.save(booking)).thenReturn(booking);
        Mockito
                .when(mapper.mapToDto(booking)).thenReturn(expected);

        BookingResponse actual = bookingService.updateApproved(OWNER_ID, dto.getId(), false);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void updateApproved_whenReject_andBookingAlreadyRejected_throws() {
        BookingRequest dto = getBookingRequest(ITEM_ID, BOOKER_ID);
        Booking booking = fromBookingRequest(dto);
        booking.setStatus(BookingStatus.REJECTED);
        Mockito
                .when(bookingRepository.findBookingByIdItemFetched(dto.getId()))
                .thenReturn(Optional.of(booking));
        Mockito
                .doThrow(ServiceException.class)
                .when(bookingDataValidator).throwIfBookingAlreadyRejected(OWNER_ID, booking);

        assertThrows(ServiceException.class,
                () -> bookingService.updateApproved(OWNER_ID, dto.getId(), false));
    }

    @Test
    void updateApproved_whenApprove_andBookingNotApproved_returnsApprovedBooking() {
        BookingRequest dto = getBookingRequest(ITEM_ID, BOOKER_ID);
        dto.setStatus(BookingStatus.WAITING);
        Booking booking = fromBookingRequest(dto);
        BookingResponse expected = fromBooking(booking);
        expected.setStatus(BookingStatus.APPROVED);

        Mockito
                .when(bookingRepository.findBookingByIdItemFetched(dto.getId()))
                        .thenReturn(Optional.of(booking));
        Mockito
                .when(bookingRepository.save(booking)).thenReturn(booking);
        Mockito
                .when(mapper.mapToDto(booking)).thenReturn(expected);

        BookingResponse actual = bookingService.updateApproved(OWNER_ID, dto.getId(), true);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void updateApproved_whenApprove_andBookingAlreadyApproved_throws() {
        BookingRequest dto = getBookingRequest(ITEM_ID, BOOKER_ID);
        Booking booking = fromBookingRequest(dto);
        booking.setStatus(BookingStatus.APPROVED);
        Mockito
                .when(bookingRepository.findBookingByIdItemFetched(dto.getId()))
                .thenReturn(Optional.of(booking));
        Mockito
                .doThrow(ServiceException.class)
                .when(bookingDataValidator).throwIfBookingAlreadyApproved(OWNER_ID, booking);
        assertThrows(ServiceException.class,
                () -> bookingService.updateApproved(OWNER_ID, dto.getId(), true));
    }

    @Test
    void createBooking_whenAllCorrect_returnsCreatedBooking() {
        BookingRequest dto = getBookingRequest(ITEM_ID, BOOKER_ID);
        Booking booking = fromBookingRequest(dto);
        BookingResponse expected = fromBooking(booking);
        Mockito
                .when(mapper.mapToDomain(dto)).thenReturn(booking);
        Mockito
                .when(bookingRepository.save(booking)).thenReturn(booking);

        Mockito
                .when(mapper.mapToDto(booking)).thenReturn(expected);

        BookingResponse actual = bookingService.createBooking(dto);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createBooking_whenItemOwnerIsTheBooker_throws() {
        BookingRequest dto = getBookingRequest(ITEM_ID, BOOKER_ID);
        Booking domain = getBooking();
        Mockito
                .when(mapper.mapToDomain(dto)).thenReturn(domain);
        Mockito
                .doThrow(ServiceException.class)
                .when(bookingDataValidator).throwIfBookerIsItemOwner(dto.getBookerId(), domain);
        assertThrows(ServiceException.class, () -> bookingService.createBooking(dto));
    }

    @Test
    void createBooking_whenItemNotFound_throws() {
        BookingRequest dto = getBookingRequest(ITEM_ID, BOOKER_ID);
        Booking domain = getBooking();
        Mockito
                .when(mapper.mapToDomain(dto)).thenReturn(domain);
        Mockito
                .doThrow(ServiceException.class)
                .when(bookingDataValidator).throwIfItemNotAvailable(dto.getItemId(), domain);
        assertThrows(ServiceException.class, () -> bookingService.createBooking(dto));
    }

}