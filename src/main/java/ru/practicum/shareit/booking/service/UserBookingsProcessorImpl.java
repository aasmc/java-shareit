package ru.practicum.shareit.booking.service;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.util.OffsetBasedPageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static ru.practicum.shareit.booking.dto.BookingStateDto.*;

@Component
public class UserBookingsProcessorImpl implements UserBookingsProcessor {

    private final BookingRepository bookingRepository;

    private Map<BookingStateDto, Function<QueryParams, List<Booking>>> stateToBookerProcessor;

    private Map<BookingStateDto, Function<QueryParams, List<Booking>>> stateToOwnerProcessor;

    public UserBookingsProcessorImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
        initProcessors();
    }

    @Override
    public List<Booking> getAllBookingsOfUser(boolean isOwner,
                                              Long userId,
                                              BookingStateDto state,
                                              int from,
                                              int size) {
        QueryParams params = getQueryParams(userId, from, size);
        return isOwner ? getBookings(state, params, stateToOwnerProcessor) :
                getBookings(state, params, stateToBookerProcessor);
    }

    private List<Booking> getBookings(BookingStateDto state,
                                      QueryParams params,
                                      Map<BookingStateDto, Function<QueryParams, List<Booking>>> map) {
        Function<QueryParams, List<Booking>> processor = Optional.ofNullable(map.get(state)).orElseThrow(() -> {
            String msg = String.format("Unknown state: %s", state);
            return new ServiceException(HttpStatus.BAD_REQUEST.value(), msg);
        });
        return processor.apply(params);
    }


    private QueryParams getQueryParams(Long userId, int from, int size) {
        Pageable pageable = new OffsetBasedPageRequest(from, size);
        LocalDateTime now = LocalDateTime.now();
        return QueryParams.builder()
                .now(now)
                .userId(userId)
                .pageable(pageable)
                .build();
    }

    private void initProcessors() {
        stateToBookerProcessor = Map.of(
                ALL, params -> bookingRepository.findAllByBookerIdOrderByStartDesc(
                        params.getUserId(), params.getPageable()
                ),
                CURRENT, params -> bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        params.getUserId(), params.getNow(), params.getNow(), params.getPageable()
                ),
                PAST, params -> bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(
                        params.getUserId(), params.getNow(), params.getPageable()
                ),
                FUTURE, params -> bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(
                        params.getUserId(), params.getNow(), params.getPageable()
                ),
                WAITING, params -> bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                        params.getUserId(), BookingStatus.WAITING, params.getPageable()
                ),
                REJECTED, params -> bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                        params.getUserId(), BookingStatus.REJECTED, params.getPageable()
                )

        );

        stateToOwnerProcessor = Map.of(
                ALL, params -> bookingRepository.findAllByItemOwnerIdOrderByStartDesc(
                        params.getUserId(), params.getPageable()
                ),
                CURRENT, params -> bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        params.getUserId(), params.getNow(), params.getNow(), params.getPageable()
                ),
                PAST, params -> bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(
                        params.getUserId(), params.getNow(), params.getPageable()
                ),
                FUTURE, params -> bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(
                        params.getUserId(), params.getNow(), params.getPageable()
                ),
                WAITING, params -> bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                        params.getUserId(), BookingStatus.WAITING, params.getPageable()
                ),
                REJECTED, params -> bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                        params.getUserId(), BookingStatus.REJECTED, params.getPageable()
                )

        );
    }

    @Builder
    @Data
    private static class QueryParams {
        private Long userId;
        private Pageable pageable;
        private LocalDateTime now;
    }
}
