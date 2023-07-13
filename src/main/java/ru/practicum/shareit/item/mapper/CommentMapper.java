package ru.practicum.shareit.item.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.Mapper;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommentMapper implements Mapper<Comment, CommentRequest, CommentResponse> {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public CommentResponse mapToDto(Comment domain) {
        return CommentResponse.builder()
                .id(domain.getId())
                .text(domain.getText())
                .authorName(domain.getAuthor().getName())
                .created(domain.getCreated())
                .build();
    }

    @Override
    public Comment mapToDomain(CommentRequest dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> {
                    String msg = String.format("User with ID=%d not found.", dto.getUserId());
                    return new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
                });
        Item item = itemRepository.findItemByIdWithBookingsFetched(dto.getItemId())
                .orElseThrow(() -> {
                    String msg = String.format("Item with ID=%d not found.", dto.getItemId());
                    return new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
                });
        checkUserBookedItem(dto, item);
        return Comment.builder()
                .text(dto.getText())
                .item(item)
                .author(user)
                .build();
    }

    private void checkUserBookedItem(CommentRequest dto, Item item) {
        LocalDateTime now = LocalDateTime.now();
        Optional<Booking> booking = item.getBookings().stream()
                .filter(b -> b.getBooker().getId().equals(dto.getUserId())
                        && b.getStatus().equals(BookingStatus.APPROVED)
                        && b.getEnd().isBefore(now))
                .findFirst();
        if (booking.isEmpty()) {
            String msg = String.format("User with ID=%d cannot post comment to item with ID=%d " +
                    "because the user never booked the item.", dto.getUserId(), dto.getItemId());
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), msg);
        }
    }
}
