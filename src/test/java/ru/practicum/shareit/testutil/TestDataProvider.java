package ru.practicum.shareit.testutil;

import ru.practicum.shareit.booking.dto.BookerResponse;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.dto.ItemBookingResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.dto.ItemResponse;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.testutil.TestConstants.*;

public class TestDataProvider {

    public static BookingResponse fromBooking(Booking domain) {
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

    public static Booking fromBookingRequest(BookingRequest req) {
        Item item = getAvailableItemWithoutBookings();
        item.setId(req.getItemId());
        return Booking.builder()
                .id(req.getId())
                .start(req.getStart())
                .end(req.getEnd())
                .status(req.getStatus())
                .booker(getMockUser(req.getBookerId()))
                .item(item)
                .build();
    }

    public static BookingRequest getBookingRequestForCreate(LocalDateTime start,
                                                            LocalDateTime end,
                                                            Long itemId) {
        return BookingRequest.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
    }

    public static BookingRequest getBookingRequest(Long itemId, Long bookerId) {
        return BookingRequest.builder()
                .id(BOOKING_ID)
                .start(BOOKING_REQUEST_START)
                .end(BOOKING_REQUEST_END)
                .itemId(itemId)
                .bookerId(bookerId)
                .status(BookingStatus.APPROVED)
                .build();
    }

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
                .request(getDefaultItemRequest())
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

    public static ItemDto getItemDtoForUpdate(boolean available) {
        return ItemDto.builder()
                .description("New Description")
                .name("New Name")
                .available(available)
                .build();
    }

    public static ItemDto getItemDtoForCreate(Long requestId) {
        return ItemDto.builder()
                .description("Description")
                .name("Name")
                .available(true)
                .requestId(requestId)
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

    public static ItemRequest getItemRequest(Long id,
                                             User requestor) {
        return new ItemRequest()
                .setId(id)
                .setRequestor(requestor)
                .setDescription("Description")
                .setCreated(ITEM_REQUEST_CREATED);
    }

    public static ItemRequest getItemRequestFromDto(ItemRequestDto dto,
                                                    User requestor) {
        return new ItemRequest()
                .setRequestor(requestor)
                .setDescription(dto.getDescription());
    }

    public static ItemRequestResponse getItemRequestResponse(ItemRequest domain,
                                                             List<ItemResponse> items) {
        return ItemRequestResponse.builder()
                .id(domain.getId())
                .description(domain.getDescription())
                .created(domain.getCreated())
                .items(items)
                .build();
    }

    public static ItemResponse getItemResponseFromItem(Item item, Long requestId) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .available(item.getAvailable())
                .description(item.getDescription())
                .requestId(requestId)
                .build();
    }

    public static Item getItemWithItemRequest(Long id,
                                              ItemRequest req,
                                              User owner,
                                              boolean available) {
        return Item.builder()
                .request(req)
                .id(id)
                .owner(owner)
                .name("Item Name")
                .description("Item Description")
                .available(available)
                .build();
    }

    public static ItemRequest getDefaultItemRequest() {
        return new ItemRequest()
                .setId(ITEM_REQUEST_ID)
                .setDescription("Description")
                .setRequestor(getMockUser(ITEM_REQUESTOR_ID))
                .setCreated(ITEM_REQUEST_CREATED);

    }

    public static Item getItemNoBookingsNoRequest(Long id, boolean available, User owner) {
        return new Item()
                .setId(id)
                .setName("Item Name")
                .setDescription("Item Description")
                .setAvailable(available)
                .setOwner(owner);
    }

    public static Item getAvailableItemWithoutBookings() {
        return new Item()
                .setId(ITEM_ID)
                .setOwner(getMockUser(OWNER_ID))
                .setName("Item Name")
                .setDescription("Item Description")
                .setAvailable(true)
                .setRequest(getDefaultItemRequest());
    }

    public static Item getTransientAvailableItemNoBookingsNoRequest(User owner) {
        return new Item()
                .setOwner(owner)
                .setName("Item Name")
                .setDescription("Item Description")
                .setAvailable(true);
    }

    public static Item getTransientAvailableItemNoBookings(User owner, ItemRequest request) {
        return new Item()
                .setOwner(owner)
                .setName("Item Name")
                .setDescription("Item Description")
                .setRequest(request)
                .setAvailable(true);
    }

    public static Booking getNextBooking() {
        Booking booking = getBooking();
        booking.setStart(booking.getStart().plusDays(10));
        booking.setStatus(BookingStatus.APPROVED);
        return booking;
    }

    public static Booking getBookingBeforeSave(User booker, Item item) {
        return new Booking()
                .setStart(BOOKING_START)
                .setEnd(BOOKING_END)
                .setItem(item)
                .setBooker(booker)
                .setStatus(BookingStatus.APPROVED);
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

    public static ItemRequestDto getitemRequestDto(Long requestorId) {
        return ItemRequestDto.builder()
                .requestorId(requestorId)
                .description("Item Description")
                .build();
    }
}
