package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.testutil.TestConstants.ITEM_ID;
import static ru.practicum.shareit.testutil.TestDataProvider.*;

@ExtendWith(MockitoExtension.class)
class BookingDataValidatorImplTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private BookingDataValidatorImpl dataValidator;

    @Test
    void throwIfBookingAlreadyApproved_throws() {
        User owner = getMockUser(10L);
        Item item = getItemNoBookingsNoRequest(ITEM_ID, true, owner);
        Booking booking = getBookingBeforeSave(owner, item);
        booking.setStatus(BookingStatus.APPROVED);

        assertThrows(ServiceException.class,
                () -> dataValidator.throwIfBookingAlreadyApproved(owner.getId(), booking)
        );
    }

    @Test
    void throwIfBookingAlreadyRejected_throws() {
        User owner = getMockUser(10L);
        Item item = getItemNoBookingsNoRequest(ITEM_ID, true, owner);
        Booking booking = getBookingBeforeSave(owner, item);
        booking.setStatus(BookingStatus.REJECTED);

        assertThrows(ServiceException.class,
                () -> dataValidator.throwIfBookingAlreadyRejected(owner.getId(), booking)
        );
    }

    @Test
    void throwIfBookerIsItemOwner_throws() {
        User owner = getMockUser(10L);
        Item item = getItemNoBookingsNoRequest(ITEM_ID, true, owner);
        Booking booking = getBookingBeforeSave(owner, item);

        assertThrows(ServiceException.class,
                () -> dataValidator.throwIfBookerIsItemOwner(owner.getId(), booking)
        );
    }

    @Test
    void throwIfUserNotExists_throws() {
        Long bookerId = 10L;
        Mockito
                .when(userRepository.existsById(bookerId))
                .thenReturn(false);

        assertThrows(ServiceException.class,
                () -> dataValidator.throwIfUserNotExists(bookerId)
        );
    }

    @Test
    void throwIfItemNotAvailable_throws() {
        User owner = getMockUser(10L);
        Item item = getItemNoBookingsNoRequest(ITEM_ID, false, owner);
        User booker = getMockUser(100L);
        Booking booking = getBookingBeforeSave(booker, item);

        assertThrows(ServiceException.class,
                () -> dataValidator.throwIfItemNotAvailable(item.getId(), booking)
        );
    }

    @Test
    void throwIfNotOwnerOfBookedItem_throws() {
        User owner = getMockUser(10L);
        Item item = getItemNoBookingsNoRequest(ITEM_ID, true, owner);
        User booker = getMockUser(100L);
        Booking booking = getBookingBeforeSave(booker, item);

        assertThrows(ServiceException.class,
                () -> dataValidator.throwIfNotOwnerOfBookedItem(1L, booking.getId(), booking)
        );
    }

}