package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.dto.ItemResponse;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.testutil.TestConstants.ITEM_REQUESTOR_ID;
import static ru.practicum.shareit.testutil.TestConstants.ITEM_REQUEST_ID;
import static ru.practicum.shareit.testutil.TestDataProvider.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestMapperTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private ItemRequestMapper mapper;

    @Test
    void mapToDto_returnsCorrectResponse() {
        User requestor = getMockUser(ITEM_REQUESTOR_ID);
        ItemRequest request = getItemRequest(ITEM_REQUEST_ID, requestor);
        List<Item> items = new ArrayList<>();
        for (long i = 1; i <= 5; i++) {
            User owner = getMockUser(i + 100);
            Item item = getItemWithItemRequest(i, request, owner, true);
            items.add(item);
        }

        Mockito
                .when(itemRepository.findAllByRequest_Id(request.getId()))
                .thenReturn(items);

        List<ItemResponse> itemResponses = items.stream().map(i -> getItemResponseFromItem(i, request.getId()))
                .collect(Collectors.toList());
        ItemRequestResponse expected = getItemRequestResponse(request, itemResponses);

        ItemRequestResponse actual = mapper.mapToDto(request);
        assertThat(actual).isEqualTo(expected);

    }

    @Test
    void mapToDomain_whenAllGood_returnsItemRequest() {
        ItemRequestDto dto = getitemRequestDto(ITEM_REQUESTOR_ID);
        User requestor = getMockUser(ITEM_REQUESTOR_ID);
        Mockito
                .when(userRepository.findById(dto.getRequestorId())).thenReturn(Optional.of(requestor));
        ItemRequest expected = getItemRequestFromDto(dto, requestor);

        ItemRequest actual = mapper.mapToDomain(dto);
        assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
        assertThat(actual.getRequestor().getId()).isEqualTo(expected.getRequestor().getId());
    }

    @Test
    void mapToDomain_whenRequestorNotFound_throws() {
        ItemRequestDto dto = getitemRequestDto(ITEM_REQUESTOR_ID);
        Mockito
                .when(userRepository.findById(dto.getRequestorId())).thenReturn(Optional.empty());
        assertThrows(ServiceException.class, () -> mapper.mapToDomain(dto));
    }

}