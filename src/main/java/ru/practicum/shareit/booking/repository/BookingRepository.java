package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.BookingView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select b from Booking b join fetch b.booker bkr join fetch b.item i where b.id =?1")
    Optional<Booking> findBookingById(Long bookingId);

    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where bkr.id = ?1 " +
            "order by b.start desc")
    List<Booking> findAllByBooker_IdOrderByStartDesc(Long bookerId);

    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where bkr.id = ?1 and " +
            "b.status = ?2 order by b.start desc")
    List<Booking> findAllByBooker_IdAndStatusEqualsOrderByStartDesc(Long bookerId, BookingStatus status);

    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where bkr.id = ?1 and " +
            "b.start < ?2 and b.end > ?3 order by b.start desc")
    List<Booking> findAllByBooker_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(Long bookerId,
                                                                                  LocalDateTime before,
                                                                                  LocalDateTime after);

    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where bkr.id = ?1 and " +
            "b.end < ?2 order by b.start desc")
    List<Booking> findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime now);


    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where bkr.id = ?1 and " +
            "b.start > ?2 order by b.start desc")
    List<Booking> findAllByBooker_IdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime now);

    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where i.owner.id = ?1 " +
            "order by b.start desc")
    List<Booking> findAllByItem_Owner_IdOrderByStartDesc(Long ownerId);

    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where i.owner.id = ?1 " +
            "and b.status = ?2 order by b.start desc")
    List<Booking> findAllByItem_Owner_IdAndStatusEqualsOrderByStartDesc(Long ownerId, BookingStatus status);

    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where i.owner.id = ?1 " +
            "and b.start < ?2 and b.end > ?3 order by b.start desc")
    List<Booking> findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(Long ownerId,
                                                                                      LocalDateTime before,
                                                                                      LocalDateTime after);

    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where i.owner.id = ?1 " +
            "and b.end < ?2 order by b.start desc")
    List<Booking> findAllByItem_Owner_IdAndEndIsBeforeOrderByStartDesc(Long ownerId, LocalDateTime now);


    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where i.owner.id = ?1 " +
            "and b.start > ?2 order by b.start desc")
    List<Booking> findAllByItem_Owner_IdAndStartIsAfterOrderByStartDesc(Long ownerId, LocalDateTime now);

    @Query("select new ru.practicum.shareit.booking.model.BookingView(b.id, b.booker.id) from Booking b " +
            "where b.item.owner.id = ?1 and b.item.id = ?2 and b.end < ?3 order by b.end desc")
    Page<BookingView> findLastBookings(Long ownerId, Long itemId, LocalDateTime now, Pageable pageable);

    @Query("select new ru.practicum.shareit.booking.model.BookingView(b.id, b.booker.id) from Booking b " +
            " where b.item.owner.id = ?1 and b.item.id = ?2 and b.start > ?3 order by b.start asc")
    Page<BookingView> findNextBookings(Long ownerId, Long itemId, LocalDateTime now, Pageable pageable);

    default Optional<BookingView> findLastBooking(Long ownerId, Long itemId, LocalDateTime now) {
        Page<BookingView> lastBookings = findLastBookings(ownerId, itemId, now, PageRequest.of(0, 1));
        if (lastBookings.hasContent()) {
            return Optional.of(lastBookings.getContent().get(0));
        }
        return Optional.empty();
    }

    default Optional<BookingView> findNextBooking(Long ownerId, Long itemId, LocalDateTime now) {
        Page<BookingView> nextBookings = findNextBookings(ownerId, itemId, now, PageRequest.of(0, 1));
        if (nextBookings.hasContent()) {
            return Optional.of(nextBookings.getContent().get(0));
        }
        return Optional.empty();
    }
}
