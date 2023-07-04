package ru.practicum.shareit.booking.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.Mapper;

@Component
@RequiredArgsConstructor
public class BookingMapper implements Mapper<Booking, BookingDto> {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    @Override
    public BookingDto mapToDto(Booking domain) {
        return BookingDto.builder()
                .id(domain.getId())
                .start(domain.getStart())
                .end(domain.getEnd())
                .itemId(domain.getItem().getId())
                .bookerId(domain.getBooker().getId())
                .build();
    }

    @Override
    public Booking mapToDomain(BookingDto dto) {
        User booker = userRepository.findById(dto.getBookerId())
                .orElseThrow(() -> {
                    String msg = String.format("User with ID=%d not found.", dto.getBookerId());
                    return new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
                });
        Item item = itemRepository.findById(dto.getItemId())
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
