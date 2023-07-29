package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.item.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.OffsetBasedPageRequest;

import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.testutil.TestDataProvider.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CommentService commentService;
    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void getItemsForUser_whenHasManyItems_returnsOnlyThoseFittingInPage() {
        int start = 1;
        int size = 2;
        Pageable pageable = new OffsetBasedPageRequest(start, size);

        Item item = getAvailableItemWithoutBookings();
        Item item2 = getAvailableItemWithoutBookings();
        item2.setId(item.getId() + 1);
        Item item3 = getAvailableItemWithoutBookings();
        item3.setId(item2.getId() + 1);
        Item item4 = getAvailableItemWithoutBookings();
        item4.setId(item3.getId() + 1);

        Mockito
                .when(itemRepository.findAllByOwnerIdFetchBookings(OWNER_ID, pageable))
                .thenReturn(List.of(item2, item3));

        Mockito
                .when(commentService.getItemIdToComments(Set.of(item2.getId(), item3.getId())))
                .thenReturn(Map.of());
        ItemDto mapped1 = itemDtoFromDomain(item2);
        ItemDto mapped2 = itemDtoFromDomain(item3);
        List<ItemDto> expected = List.of(mapped1, mapped2);

        Mockito
                .when(itemMapper.mapToDto(item2)).thenReturn(mapped1);
        Mockito
                .when(itemMapper.mapToDto(item3)).thenReturn(mapped2);


        List<ItemDto> itemsForUser = itemService.getItemsForUser(OWNER_ID, start, size);
        assertThat(itemsForUser).isEqualTo(expected);
    }

    @Test
    void getItemsForUser_whenUserHasItemsWithBookings_returnsCorrectList() {
        int start = 0;
        int size = 10;
        Pageable pageable = new OffsetBasedPageRequest(start, size);

        Item item = getAvailableItemWithoutBookings();

        Booking lastBooking = getBooking();
        item.getBookings().add(lastBooking);
        lastBooking.setItem(item);

        Booking nextBooking = getNextBooking();
        item.getBookings().add(lastBooking);
        nextBooking.setItem(item);

        Booking afterNextBooking = getNextBooking();
        afterNextBooking.setStart(afterNextBooking.getStart().plusDays(1));
        afterNextBooking.setEnd(afterNextBooking.getEnd().plusDays(2));
        item.getBookings().add(afterNextBooking);
        afterNextBooking.setItem(item);

        Booking beforeLastBooking = getBooking();
        beforeLastBooking.setStart(beforeLastBooking.getStart().minusDays(10));
        beforeLastBooking.setEnd(beforeLastBooking.getEnd().minusDays(5));
        item.getBookings().add(beforeLastBooking);
        beforeLastBooking.setItem(item);

        BookingResponseDto last = fromBookingDomain(lastBooking);
        BookingResponseDto next = fromBookingDomain(nextBooking);
        List<CommentResponse> commentResponseList = getCommentResponseList();
        Map<Long, List<CommentResponse>> itemIdToComments = Map.of(
                item.getId(), commentResponseList
        );

        Mockito
                .when(itemRepository.findAllByOwnerIdFetchBookings(OWNER_ID, pageable))
                .thenReturn(List.of(item));

        Mockito
                .when(commentService.getItemIdToComments(Set.of(item.getId())))
                .thenReturn(itemIdToComments);
        ItemDto mapped = itemDtoFromDomain(item);

        Mockito
                .when(itemMapper.mapToDto(item)).thenReturn(mapped);

        ItemDto expected = itemDtoFromDomainWithBookings(item, last, next);
        expected.setComments(commentResponseList);

        List<ItemDto> itemsForUser = itemService.getItemsForUser(OWNER_ID, start, size);
        assertThat(itemsForUser.size()).isEqualTo(1);
        assertThat(itemsForUser.get(0)).isEqualTo(expected);
    }

    @Test
    void getItemsForUser_whenNoItems_returnsEmptyList() {
        int start = 0;
        int size = 10;
        Pageable pageable = new OffsetBasedPageRequest(start, size);

        Mockito
                .when(itemRepository.findAllByOwnerIdFetchBookings(OWNER_ID, pageable))
                .thenReturn(Collections.emptyList());
        Mockito
                .when(commentService.getItemIdToComments(Set.of()))
                .thenReturn(Map.of());

        List<ItemDto> itemsForUser = itemService.getItemsForUser(OWNER_ID, start, size);
        assertThat(itemsForUser).isEmpty();
    }

    @Test
    void saveItem_savesItem() {
        ItemDto dto = getItemDtoRequest(ITEM_REQUEST_ID);
        Item item = itemFromDto(dto);
        Mockito
                .when(itemMapper.mapToDomain(dto)).thenReturn(item);
        Mockito
                .when(itemRepository.save(item)).thenReturn(item);

        ItemDto expected = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(item.getOwner().getId())
                .requestId(item.getRequest() == null ? null : item.getRequest().getId())
                .build();

        ItemDto saved = itemService.saveItem(dto);
        assertThat(saved).isEqualTo(expected);
    }

    @Test
    void update_whenAllCorrect_ItemUpdated() {
        ItemDto dto = getItemDtoRequest(ITEM_REQUEST_ID);
        Item item = itemFromDto(dto);
        dto.setName("New Name");
        dto.setDescription("New Description");
        dto.setAvailable(false);
        dto.setId(item.getId());
        User userRef = User.builder().id(OWNER_ID).build();
        Mockito
                .when(itemRepository.findById(dto.getId())).thenReturn(Optional.of(item));
        Mockito
                .when(userRepository.getReferenceById(OWNER_ID)).thenReturn(userRef);
        ItemDto updated = itemService.update(dto, OWNER_ID);
        assertThat(updated).isEqualTo(dto);
    }

    @Test
    void update_whenItemNotBelongsToUser_thenThrows() {
        ItemDto dto = getItemDtoRequest(ITEM_REQUEST_ID);
        Item item = itemFromDto(dto);
        User userRef = User.builder().id(OWNER_ID + 1).build();
        Mockito
                .when(itemRepository.findById(dto.getId())).thenReturn(Optional.of(item));
        Mockito
                .when(userRepository.getReferenceById(OWNER_ID + 1)).thenReturn(userRef);

        assertThrows(ServiceException.class, () -> itemService.update(dto, OWNER_ID + 1));
    }

    @Test
    void update_whenOwnerNotFound_throws() {
        ItemDto dto = getItemDtoRequest(ITEM_REQUEST_ID);
        Item item = itemFromDto(dto);
        User userRef = User.builder().build();
        Mockito
                .when(itemRepository.findById(dto.getId())).thenReturn(Optional.of(item));
        Mockito
                .when(userRepository.getReferenceById(OWNER_ID)).thenReturn(userRef);

        assertThrows(ServiceException.class, () -> itemService.update(dto, OWNER_ID));
    }

    @Test
    void update_whenItemNotFound_throws() {
        ItemDto dto = getItemDtoRequest(ITEM_REQUEST_ID);
        Mockito
                .when(itemRepository.findById(dto.getId())).thenReturn(Optional.empty());
        assertThrows(ServiceException.class, () -> itemService.update(dto, OWNER_ID));
    }

    @Test
    void findById_whenHasItem_returnsCorrectDto() {
        Item item = getAvailableItemWithoutBookings();
        Booking lastBooking = getBooking();
        item.getBookings().add(lastBooking);
        lastBooking.setItem(item);
        Booking nextBooking = getNextBooking();
        item.getBookings().add(lastBooking);
        nextBooking.setItem(item);
        BookingResponseDto last = fromBookingDomain(lastBooking);
        BookingResponseDto next = fromBookingDomain(nextBooking);
        List<CommentResponse> commentResponseList = getCommentResponseList();
        Mockito
                .when(commentService.getCommentsOfItem(item.getId()))
                        .thenReturn(commentResponseList);
        Mockito
                .when(itemRepository.findItemByIdWithBookingsFetched(ITEM_ID))
                .thenReturn(Optional.of(item));

        ItemDto dto = itemDtoFromDomainWithBookings(item, last, next);
        dto.setComments(commentResponseList);

        Mockito
                .when(itemMapper.mapToDto(item)).thenReturn(dto);
        Mockito
                .when(commentService.getCommentsOfItem(item.getId()))
                .thenReturn(Collections.emptyList());

        ItemDto byId = itemService.findById(ITEM_ID, OWNER_ID);
        assertThat(byId).isEqualTo(dto);
    }

    @Test
    void findById_whenNoItem_throws() {
        Mockito
                .when(itemRepository.findItemByIdWithBookingsFetched(ITEM_ID))
                .thenReturn(Optional.empty());
        assertThrows(ServiceException.class, () -> itemService.findById(ITEM_ID, OWNER_ID));
    }

}