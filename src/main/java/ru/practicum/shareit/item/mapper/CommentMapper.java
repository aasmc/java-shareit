package ru.practicum.shareit.item.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
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
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Predicate;

import static ru.practicum.shareit.booking.model.BookingStatus.APPROVED;

@Component
@RequiredArgsConstructor
public class CommentMapper implements Mapper<Comment, CommentRequest, CommentResponse> {

    private static final long CREATION_DELTA_MILLIS = 500L;

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
                .created(getCreationDate())
                .author(user)
                .build();
    }

    /**
     * Необходимо вручную проставлять дату создания вместо использования
     * стандартного механизма org.hibernate.annotations.CreationTimestamp
     * иначе тесты в постмане не проходят, так как время сохранения в БД
     * точно совпадает со временем запроса, а тесты ждут, что сохранение
     * произойдет чуть позже((
     */
    private LocalDateTime getCreationDate() {
        return LocalDateTime.now()
                .plus(CREATION_DELTA_MILLIS, ChronoUnit.MILLIS);
    }

    private void checkUserBookedItem(CommentRequest dto, Item item) {
        Optional<Booking> booking = item.getBookings().stream()
                .filter(hasUserBookedAnItem(dto.getUserId()))
                .findFirst();
        if (booking.isEmpty()) {
            String msg = String.format("User with ID=%d cannot post comment to item with ID=%d " +
                    "because the user never booked the item.", dto.getUserId(), dto.getItemId());
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), msg);
        }
    }

    private Predicate<Booking> hasUserBookedAnItem(Long userId) {
        return b -> b.getBooker().getId().equals(userId)
                && b.getStatus().equals(APPROVED)
                && b.getEnd().isBefore(LocalDateTime.now());
    }
}
