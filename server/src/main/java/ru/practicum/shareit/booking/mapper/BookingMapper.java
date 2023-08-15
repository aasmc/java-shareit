package ru.practicum.shareit.booking.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookerResponse;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.dto.ItemBookingResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.Mapper;

@Component
@RequiredArgsConstructor
public class BookingMapper implements Mapper<Booking, BookingRequest, BookingResponse> {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingResponse mapToDto(Booking domain) {
        return BookingResponse.builder()
                .id(domain.getId())
                .start(domain.getStart())
                .end(domain.getEnd())
                .status(domain.getStatus())
                .booker(BookerResponse.builder()
                        .id(domain.getBooker().getId())
                        .build())
                .item(ItemBookingResponse.builder()
                        .id(domain.getItem().getId())
                        .name(domain.getItem().getName())
                        .build())
                .build();
    }

    @Override
    public Booking mapToDomain(BookingRequest dto) {
        User booker = userRepository.findById(dto.getBookerId())
                .orElseThrow(() -> {
                    String msg = String.format("User with ID=%d not found.", dto.getBookerId());
                    return new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
                });
        Item item = itemRepository.findByIdOwnerFetched(dto.getItemId())
                .orElseThrow(() -> {
                   String msg = String.format("Item with ID=%d not found.", dto.getItemId());
                   return new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
                });
        return Booking.builder()
                .id(dto.getId())
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(item)
                .booker(booker)
                .status(dto.getStatus())
                .build();
    }
}
