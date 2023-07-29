package ru.practicum.shareit.testutil;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;

public class TestDataProvider {

    public static final Long BOOKER_ID = 1L;
    public static final Long BOOKING_ID = 2L;
    public static final Long ITEM_ID = 3L;
    public static final Long OWNER_ID = 4L;
    public static final Long ITEM_REQUESTOR_ID = 5L;
    public static final Long ITEM_REQUEST_ID = 6L;
    public static final Long ITEM_DTO_ID = 7L;

    public static final LocalDateTime BOOKING_START = LocalDateTime.of(
            LocalDate.of(2023, Month.JULY, 27),
            LocalTime.of(12, 0)
    );

    public static final LocalDateTime BOOKING_END = LocalDateTime.of(
            LocalDate.of(2023, Month.JULY, 28),
            LocalTime.of(12, 0)
    );

    public static final LocalDateTime ITEM_REQUEST_CREATED = LocalDateTime.of(
            LocalDate.of(2023, Month.JULY, 25),
            LocalTime.of(12, 0)
    );

    public static List<CommentResponse> getCommentResponseList() {
        return List.of(
                CommentResponse.builder().id(1L).build(),
                CommentResponse.builder().id(2L).build()
        );
    }

    public static Item itemFromDto(ItemDto dto) {
        return Item.builder()
                .id(ITEM_ID)
                .owner(getMockUser(dto.getOwnerId()))
                .request(getItemRequest())
                .description(dto.getDescription())
                .name(dto.getName())
                .available(true)
                .build();
    }

    public static ItemDto itemDtoFromDomainWithBookings(Item item,
                                                        BookingResponseDto last,
                                                        BookingResponseDto next) {
        ItemDto dto = itemDtoFromDomain(item);
        dto.setLastBooking(last);
        dto.setNextBooking(next);
        return dto;
    }

    public static BookingResponseDto fromBookingDomain(Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }

    public static ItemDto itemDtoFromDomain(Item item) {
        return ItemDto.builder()
                .ownerId(OWNER_ID)
                .name(item.getName())
                .description(item.getDescription())
                .id(item.getId())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public static CommentResponse fromDomain(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName("AuthorName")
                .created(comment.getCreated())
                .build();
    }

    public static Comment getComment(Long id, Long itemId) {
        return Comment.builder()
                .id(id)
                .item(Item.builder().id(itemId).build())
                .author(null)
                .created(LocalDateTime.now())
                .build();
    }

    public static Comment getComment(CommentRequest dto, Item item, User user) {
        return Comment.builder()
                .text(dto.getText())
                .item(item)
                .created(LocalDateTime.now())
                .author(user)
                .build();
    }

    public static CommentRequest getCommentRequest(Long userId, Long itemId) {
        return CommentRequest.builder()
                .userId(userId)
                .itemId(itemId)
                .build();
    }

    public static ItemDto getItemDtoRequest(Long requestId) {
        return ItemDto.builder()
                .ownerId(OWNER_ID)
                .name("name")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();
    }

    public static ItemRequest getItemRequest() {
        return new ItemRequest()
                .setId(ITEM_REQUEST_ID)
                .setDescription("Description")
                .setRequestor(getMockUser(ITEM_REQUESTOR_ID))
                .setCreated(ITEM_REQUEST_CREATED);

    }

    public static Item getAvailableItemWithoutBookings() {
        return new Item()
                .setId(ITEM_ID)
                .setOwner(getMockUser(OWNER_ID))
                .setName("Item Name")
                .setDescription("Item Description")
                .setAvailable(true)
                .setRequest(getItemRequest());
    }

    public static Booking getNextBooking() {
        Booking booking = getBooking();
        booking.setStart(booking.getStart().plusDays(10));
        booking.setStatus(BookingStatus.APPROVED);
        return booking;
    }

    public static Booking getBooking() {
        return new Booking()
                .setId(BOOKING_ID)
                .setStart(BOOKING_START)
                .setEnd(BOOKING_END)
                .setItem(getAvailableItemWithoutBookings())
                .setBooker(getMockUser(BOOKER_ID))
                .setStatus(BookingStatus.APPROVED);
    }

    public static CommentRequest getCommentRequestDto(Long itemId, Long userId) {
        return CommentRequest.builder()
                .itemId(itemId)
                .userId(userId)
                .text("text")
                .build();
    }

    public static UserDto fromUser(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User fromDto(UserDto dto) {
        return User.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public static UserDto getUserDto() {
        return UserDto.builder()
                .email("user@user.com")
                .name("user")
                .build();
    }

    public static User getMockUser(Long id) {
        return User.builder()
                .id(id)
                .name("user")
                .email("user@user.com")
                .build();
    }

    public static UserDto expectedUserAfterUpdate() {
        return UserDto.builder()
                .email("update@user.com")
                .name("update")
                .build();
    }

}
