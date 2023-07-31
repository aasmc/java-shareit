package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.testutil.TestDataProvider.*;
import static ru.practicum.shareit.testutil.TestConstants.*;


@ExtendWith(MockitoExtension.class)
class ItemMapperTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @InjectMocks
    private ItemMapper mapper;

    @Test
    void mapToDomain_whenAllCorrect_mapsCorrectly() {
        ItemDto dto = getItemDtoRequest(ITEM_REQUEST_ID);
        User user = getMockUser(dto.getOwnerId());
        ItemRequest request = getItemRequest();
        Mockito
                .when(userRepository.findById(dto.getOwnerId())).thenReturn(Optional.of(user));
        Mockito
                .when(itemRequestRepository.findById(dto.getRequestId())).thenReturn(Optional.of(request));
        Item item = mapper.mapToDomain(dto);
        assertThat(item.getName()).isEqualTo(dto.getName());
        assertThat(item.getDescription()).isEqualTo(dto.getDescription());
        assertThat(item.getAvailable()).isEqualTo(dto.getAvailable());
        assertThat(item.getOwner().getId()).isEqualTo(user.getId());
        assertThat(item.getRequest().getId()).isEqualTo(request.getId());
    }

    @Test
    void mapToDomain_whenItemRequestNotFound_throws() {
        ItemDto dto = getItemDtoRequest(ITEM_REQUEST_ID);
        User user = getMockUser(dto.getOwnerId());
        Mockito
                .when(userRepository.findById(dto.getOwnerId())).thenReturn(Optional.of(user));
        Mockito
                .when(itemRequestRepository.findById(dto.getRequestId())).thenReturn(Optional.empty());
        assertThrows(ServiceException.class, () -> mapper.mapToDomain(dto));
    }

    @Test
    void mapToDomain_whenUserNotFound_throws() {
        ItemDto dto = getItemDtoRequest(ITEM_REQUEST_ID);
        Mockito
                .when(userRepository.findById(dto.getOwnerId())).thenReturn(Optional.empty());
        assertThrows(ServiceException.class, () -> mapper.mapToDomain(dto));
    }

}