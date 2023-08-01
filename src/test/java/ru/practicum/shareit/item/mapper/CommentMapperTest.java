package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.testutil.TestConstants.*;
import static ru.practicum.shareit.testutil.TestDataProvider.*;

@ExtendWith(MockitoExtension.class)
class CommentMapperTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private CommentMapper commentMapper;

    @Test
    void mapToDomain_whenUserBookedItem_mapsCorrectly() {
        CommentRequest dto = getCommentRequestDto(ITEM_ID, BOOKER_ID);
        User user = getMockUser(dto.getUserId());
        Item item = getAvailableItemWithoutBookings();
        Booking booking = getBooking();
        item.getBookings().add(booking);
        booking.setItem(item);
        Mockito
                .when(userRepository.findById(dto.getUserId())).thenReturn(Optional.of(user));
        Mockito
                .when(itemRepository.findItemByIdWithBookingsFetched(dto.getItemId())).thenReturn(Optional.of(item));

        Comment comment = commentMapper.mapToDomain(dto);
        assertThat(comment.getText()).isEqualTo(dto.getText());
        assertThat(comment.getItem()).isEqualTo(item);
        assertThat(comment.getAuthor().getId()).isEqualTo(dto.getUserId());
    }

    @Test
    void mapToDomain_whenUserNotBookedItem_throws() {
        CommentRequest dto = getCommentRequestDto(ITEM_ID, OWNER_ID);
        User user = getMockUser(dto.getUserId());
        Item item = getAvailableItemWithoutBookings();
        Booking booking = getBooking();
        item.getBookings().add(booking);
        Mockito
                .when(userRepository.findById(dto.getUserId())).thenReturn(Optional.of(user));
        Mockito
                .when(itemRepository.findItemByIdWithBookingsFetched(dto.getItemId())).thenReturn(Optional.of(item));
        assertThrows(ServiceException.class, () -> commentMapper.mapToDomain(dto));
    }

    @Test
    void mapToDomain_whenItemNotFound_throws() {
        CommentRequest dto = getCommentRequestDto(ITEM_ID, BOOKER_ID);
        User user = getMockUser(dto.getUserId());
        Mockito
                .when(userRepository.findById(dto.getUserId())).thenReturn(Optional.of(user));
        Mockito
                .when(itemRepository.findItemByIdWithBookingsFetched(dto.getItemId())).thenReturn(Optional.empty());
        assertThrows(ServiceException.class, () -> commentMapper.mapToDomain(dto));
    }

    @Test
    void mapToDomain_whenUserNotFound_throws() {
        CommentRequest dto = getCommentRequestDto(ITEM_ID, BOOKER_ID);
        Mockito
                .when(userRepository.findById(dto.getUserId())).thenReturn(Optional.empty());
        assertThrows(ServiceException.class, () -> commentMapper.mapToDomain(dto));
    }

}