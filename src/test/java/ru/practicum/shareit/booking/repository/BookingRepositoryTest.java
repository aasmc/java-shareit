package ru.practicum.shareit.booking.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.BaseJpaTest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static ru.practicum.shareit.booking.model.BookingStatus.APPROVED;
import static ru.practicum.shareit.booking.model.BookingStatus.CANCELED;
import static ru.practicum.shareit.testutil.TestConstants.BOOKING_END;
import static ru.practicum.shareit.testutil.TestConstants.BOOKING_START;
import static ru.practicum.shareit.testutil.TestDataProvider.getMockUser;
import static ru.practicum.shareit.testutil.TestDataProvider.getTransientAvailableItemNoBookingsNoRequest;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingRepositoryTest extends BaseJpaTest {

    private final BookingRepository bookingRepository;
    private final TestEntityManager em;

    @Test
    void findAllByItemOwnerIdAndStartAfterOrderByStartDesc_returnsCorrectList() {
        User booker = getMockUser(null);
        booker.setEmail("booker@email.com");
        booker = em.persistAndFlush(booker);
        User owner = getMockUser(null);
        owner = em.persistAndFlush(owner);

        Item itemOne = saveAndReturnItem(owner);
        Item itemTwo = saveAndReturnItem(owner);
        Item itemThree = saveAndReturnItem(owner);

        Booking one = saveAndReturnBooking(booker, // in range (second)
                itemOne,
                APPROVED,
                BOOKING_START.plusDays(1),
                BOOKING_END.plusDays(5));
        saveAndReturnBooking(booker, // not in range (END)
                itemTwo,
                APPROVED,
                BOOKING_START.minusDays(1),
                BOOKING_END);
        Booking three = saveAndReturnBooking(booker, // in range (first)
                itemThree,
                APPROVED,
                BOOKING_START.plusDays(10),
                BOOKING_START.plusDays(11));

        List<Booking> bookings = bookingRepository
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(owner.getId(),
                        BOOKING_START,
                        PageRequest.of(0, 10));

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getId()).isEqualTo(three.getId());
        assertThat(bookings.get(1).getId()).isEqualTo(one.getId());
    }

    @Test
    void findAllByItemOwnerIdAndEndBeforeOrderByStartDesc_returnsCorrectList() {
        User booker = getMockUser(null);
        booker.setEmail("booker@email.com");
        booker = em.persistAndFlush(booker);
        User owner = getMockUser(null);
        owner = em.persistAndFlush(owner);

        Item itemOne = saveAndReturnItem(owner);
        Item itemTwo = saveAndReturnItem(owner);
        Item itemThree = saveAndReturnItem(owner);

        Booking one = saveAndReturnBooking(booker, // in range
                itemOne,
                APPROVED,
                BOOKING_START.minusDays(10),
                BOOKING_START.minusDays(5));
        saveAndReturnBooking(booker, // not in range (END)
                itemTwo,
                APPROVED,
                BOOKING_START,
                BOOKING_START.plusDays(2));
        Booking three = saveAndReturnBooking(booker, // in range
                itemThree,
                APPROVED,
                BOOKING_START.minusDays(2),
                BOOKING_START.minusDays(1));

        List<Booking> bookings = bookingRepository
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(owner.getId(),
                        BOOKING_START,
                        PageRequest.of(0, 10));

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getId()).isEqualTo(three.getId());
        assertThat(bookings.get(1).getId()).isEqualTo(one.getId());
    }

    @Test
    void findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc_returnsCorrectList() {
        User booker = getMockUser(null);
        booker.setEmail("booker@email.com");
        booker = em.persistAndFlush(booker);
        User owner = getMockUser(null);
        owner = em.persistAndFlush(owner);

        Item itemOne = saveAndReturnItem(owner);
        Item itemTwo = saveAndReturnItem(owner);
        Item itemThree = saveAndReturnItem(owner);

        Booking one = saveAndReturnBooking(booker, // in range
                itemOne,
                APPROVED,
                BOOKING_START.minusDays(1),
                BOOKING_END);
        saveAndReturnBooking(booker, // not in range (START)
                itemTwo,
                APPROVED,
                BOOKING_START,
                BOOKING_END.plusDays(2));
        Booking three = saveAndReturnBooking(booker, // in range
                itemThree,
                APPROVED,
                BOOKING_START.minusDays(2),
                BOOKING_END.plusDays(3));

        List<Booking> bookings = bookingRepository
                .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(owner.getId(),
                        BOOKING_START,
                        BOOKING_START,
                        PageRequest.of(0, 10));

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getId()).isEqualTo(one.getId());
        assertThat(bookings.get(1).getId()).isEqualTo(three.getId());
    }

    @Test
    void findAllByItemOwnerIdAndStatusOrderByStartDesc_returnsCorrectList() {
        User booker = getMockUser(null);
        booker.setEmail("booker@email.com");
        booker = em.persistAndFlush(booker);
        User owner = getMockUser(null);
        owner = em.persistAndFlush(owner);

        Item itemOne = saveAndReturnItem(owner);
        Item itemTwo = saveAndReturnItem(owner);
        Item itemThree = saveAndReturnItem(owner);

        Booking one = saveAndReturnBooking(booker, itemOne, APPROVED, BOOKING_START, BOOKING_END);
        Booking two = saveAndReturnBooking(booker,
                itemTwo,
                APPROVED,
                BOOKING_START.plusDays(1),
                BOOKING_END.plusDays(1));

        saveAndReturnBooking(booker,
                itemThree,
                CANCELED,
                BOOKING_START.plusDays(1),
                BOOKING_END.plusDays(1));

        List<Booking> bookings = bookingRepository
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(owner.getId(),
                        APPROVED,
                        PageRequest.of(0, 10));

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getId()).isEqualTo(two.getId());
        assertThat(bookings.get(1).getId()).isEqualTo(one.getId());
    }

    @Test
    void findAllByItemOwnerIdOrderByStartDesc_returnsCorrectList() {
        User booker = getMockUser(null);
        booker.setEmail("booker@email.com");
        booker = em.persistAndFlush(booker);
        User owner = getMockUser(null);
        owner = em.persistAndFlush(owner);

        Item itemOne = saveAndReturnItem(owner);
        Item itemTwo = saveAndReturnItem(owner);

        Booking one = saveAndReturnBooking(
                booker,
                itemOne,
                APPROVED,
                BOOKING_START,
                BOOKING_END
        );

        Booking two = saveAndReturnBooking(
                booker,
                itemTwo,
                APPROVED,
                BOOKING_START.plusDays(1),
                BOOKING_END.plusDays(1)
        );

        List<Booking> bookings = bookingRepository
                .findAllByItemOwnerIdOrderByStartDesc(owner.getId(), PageRequest.of(0, 10));

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getId()).isEqualTo(two.getId());
        assertThat(bookings.get(1).getId()).isEqualTo(one.getId());
    }

    @Test
    void findAllByBookerIdAndStartAfterOrderByStartDesc_returnsCorrectList() {
        User booker = getMockUser(null);
        booker.setEmail("booker@email.com");
        booker = em.persistAndFlush(booker);
        User owner = getMockUser(null);
        owner = em.persistAndFlush(owner);

        Item itemOne = saveAndReturnItem(owner);
        Item itemTwo = saveAndReturnItem(owner);
        Item itemThree = saveAndReturnItem(owner);

        Booking one = saveAndReturnBooking(booker, // in range (second)
                itemOne,
                APPROVED,
                BOOKING_START.plusDays(1),
                BOOKING_END.plusDays(5));
        saveAndReturnBooking(booker, // not in range (END)
                itemTwo,
                APPROVED,
                BOOKING_START.minusDays(1),
                BOOKING_END);
        Booking three = saveAndReturnBooking(booker, // in range (first)
                itemThree,
                APPROVED,
                BOOKING_START.plusDays(10),
                BOOKING_START.plusDays(11));

        List<Booking> bookings = bookingRepository
                .findAllByBookerIdAndStartAfterOrderByStartDesc(booker.getId(),
                        BOOKING_START,
                        PageRequest.of(0, 10));

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getId()).isEqualTo(three.getId());
        assertThat(bookings.get(1).getId()).isEqualTo(one.getId());
    }

    @Test
    void findAllByBookerIdAndEndBeforeOrderByStartDesc_returnsCorrectList() {
        User booker = getMockUser(null);
        booker.setEmail("booker@email.com");
        booker = em.persistAndFlush(booker);
        User owner = getMockUser(null);
        owner = em.persistAndFlush(owner);

        Item itemOne = saveAndReturnItem(owner);
        Item itemTwo = saveAndReturnItem(owner);
        Item itemThree = saveAndReturnItem(owner);

        Booking one = saveAndReturnBooking(booker, // in range
                itemOne,
                APPROVED,
                BOOKING_START.minusDays(10),
                BOOKING_START.minusDays(5));
        saveAndReturnBooking(booker, // not in range (END)
                itemTwo,
                APPROVED,
                BOOKING_START,
                BOOKING_START.plusDays(2));
        Booking three = saveAndReturnBooking(booker, // in range
                itemThree,
                APPROVED,
                BOOKING_START.minusDays(2),
                BOOKING_START.minusDays(1));

        List<Booking> bookings = bookingRepository
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(booker.getId(),
                        BOOKING_START,
                        PageRequest.of(0, 10));

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getId()).isEqualTo(three.getId());
        assertThat(bookings.get(1).getId()).isEqualTo(one.getId());
    }

    @Test
    void findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc_returnsCorrectList() {
        User booker = getMockUser(null);
        booker.setEmail("booker@email.com");
        booker = em.persistAndFlush(booker);
        User owner = getMockUser(null);
        owner = em.persistAndFlush(owner);

        Item itemOne = saveAndReturnItem(owner);
        Item itemTwo = saveAndReturnItem(owner);
        Item itemThree = saveAndReturnItem(owner);

        Booking one = saveAndReturnBooking(booker, // in range
                itemOne,
                APPROVED,
                BOOKING_START.minusDays(1),
                BOOKING_END);
        saveAndReturnBooking(booker, // not in range (START)
                itemTwo,
                APPROVED,
                BOOKING_START,
                BOOKING_END.plusDays(2));
        Booking three = saveAndReturnBooking(booker, // in range
                itemThree,
                APPROVED,
                BOOKING_START.minusDays(2),
                BOOKING_END.plusDays(3));

        List<Booking> bookings = bookingRepository
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(booker.getId(),
                        BOOKING_START,
                        BOOKING_START,
                        PageRequest.of(0, 10));

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getId()).isEqualTo(one.getId());
        assertThat(bookings.get(1).getId()).isEqualTo(three.getId());
    }

    @Test
    void findAllByBookerIdAndStatusOrderByStartDesc_returnsCorrectList() {
        User booker = getMockUser(null);
        booker.setEmail("booker@email.com");
        booker = em.persistAndFlush(booker);
        User owner = getMockUser(null);
        owner = em.persistAndFlush(owner);

        Item itemOne = saveAndReturnItem(owner);
        Item itemTwo = saveAndReturnItem(owner);
        Item itemThree = saveAndReturnItem(owner);

        Booking one = saveAndReturnBooking(booker, itemOne, APPROVED, BOOKING_START, BOOKING_END);
        Booking two = saveAndReturnBooking(booker,
                itemTwo,
                APPROVED,
                BOOKING_START.plusDays(1),
                BOOKING_END.plusDays(1));

        saveAndReturnBooking(booker,
                itemThree,
                CANCELED,
                BOOKING_START.plusDays(1),
                BOOKING_END.plusDays(1));

        List<Booking> bookings = bookingRepository
                .findAllByBookerIdAndStatusOrderByStartDesc(booker.getId(),
                        APPROVED,
                        PageRequest.of(0, 10));

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getId()).isEqualTo(two.getId());
        assertThat(bookings.get(1).getId()).isEqualTo(one.getId());
    }

    @Test
    void findAllByBookerIdOrderByStartDesc_returnsCorrectList() {
        User booker = getMockUser(null);
        booker.setEmail("booker@email.com");
        booker = em.persistAndFlush(booker);
        User owner = getMockUser(null);
        owner = em.persistAndFlush(owner);

        Item itemOne = saveAndReturnItem(owner);
        Item itemTwo = saveAndReturnItem(owner);

        Booking one = saveAndReturnBooking(
                booker,
                itemOne,
                APPROVED,
                BOOKING_START,
                BOOKING_END
        );

        Booking two = saveAndReturnBooking(
                booker,
                itemTwo,
                APPROVED,
                BOOKING_START.plusDays(1),
                BOOKING_END.plusDays(1)
        );

        List<Booking> bookings = bookingRepository
                .findAllByBookerIdOrderByStartDesc(booker.getId(), PageRequest.of(0, 10));

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getId()).isEqualTo(two.getId());
        assertThat(bookings.get(1).getId()).isEqualTo(one.getId());
    }

    private Booking saveAndReturnBooking(User booker,
                                         Item item,
                                         BookingStatus status,
                                         LocalDateTime start,
                                         LocalDateTime end) {
        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .booker(booker)
                .item(item)
                .status(status)
                .build();
        return em.persistAndFlush(booking);
    }

    private Item saveAndReturnItem(User owner) {
        Item item = getTransientAvailableItemNoBookingsNoRequest(owner);
        item.setId(null);
        item.setOwner(owner);
        return em.persistAndFlush(item);
    }

}