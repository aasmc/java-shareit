package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.BookingView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select b from Booking b join fetch b.booker bkr join fetch b.item i where b.id =:bookingId")
    Optional<Booking> findBookingByIdItemFetched(@Param("bookingId") Long bookingId);

    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where bkr.id = :bookerId " +
            "order by b.start desc")
    List<Booking> findAllByBookerIdOrderByStartDesc(@Param("bookerId") Long bookerId, Pageable pageable);

    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where bkr.id = :bookerId and " +
            "b.status = :status order by b.start desc")
    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(@Param("bookerId") Long bookerId,
                                                             @Param("status") BookingStatus status,
                                                             Pageable pageable);

    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where bkr.id = :bookerId and " +
            "b.start < :before and b.end > :after order by b.start desc")
    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(@Param("bookerId") Long bookerId,
                                                                             @Param("before") LocalDateTime before,
                                                                             @Param("after") LocalDateTime after,
                                                                             Pageable pageable);

    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where bkr.id = :bookerId and " +
            "b.end < :now order by b.start desc")
    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(@Param("bookerId") Long bookerId,
                                                                @Param("now") LocalDateTime now,
                                                                Pageable pageable);


    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where bkr.id = :bookerId and " +
            "b.start > :now order by b.start desc")
    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(@Param("bookerId") Long bookerId,
                                                                 @Param("now") LocalDateTime now,
                                                                 Pageable pageable);

    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where i.owner.id = :ownerId " +
            "order by b.start desc")
    List<Booking> findAllByItemOwnerIdOrderByStartDesc(@Param("ownerId") Long ownerId, Pageable pageable);

    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where i.owner.id = :ownerId " +
            "and b.status = :status order by b.start desc")
    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(@Param("ownerId") Long ownerId,
                                                                @Param("status") BookingStatus status,
                                                                Pageable pageable);

    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where i.owner.id = :ownerId " +
            "and b.start < :before and b.end > :after order by b.start desc")
    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(@Param("ownerId") Long ownerId,
                                                                                @Param("before") LocalDateTime before,
                                                                                @Param("after") LocalDateTime after,
                                                                                Pageable pageable);

    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where i.owner.id = :ownerId " +
            "and b.end < :now order by b.start desc")
    List<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(@Param("ownerId") Long ownerId,
                                                                   @Param("now") LocalDateTime now,
                                                                   Pageable pageable);


    @Query("select b from Booking b join fetch b.item i " +
            "join fetch b.booker bkr where i.owner.id = :ownerId " +
            "and b.start > :now order by b.start desc")
    List<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(@Param("ownerId") Long ownerId,
                                                                    @Param("now") LocalDateTime now,
                                                                    Pageable pageable);

    @Query("select new ru.practicum.shareit.booking.model.BookingView(b.id, b.booker.id) from Booking b " +
            "where b.item.owner.id = :ownerId and b.item.id = :itemId and b.end < :now order by b.end desc")
    Page<BookingView> findLastBookings(@Param("ownerId") Long ownerId,
                                       @Param("itemId") Long itemId,
                                       @Param("now") LocalDateTime now,
                                       Pageable pageable);

    @Query("select new ru.practicum.shareit.booking.model.BookingView(b.id, b.booker.id) from Booking b " +
            " where b.item.owner.id = :ownerId and b.item.id = :itemId and b.start > :now order by b.start asc")
    Page<BookingView> findNextBookings(@Param("ownerId") Long ownerId,
                                       @Param("itemId") Long itemId,
                                       @Param("now") LocalDateTime now,
                                       Pageable pageable);

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
