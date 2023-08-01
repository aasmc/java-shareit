package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.testutil.TestConstants.ITEM_REQUESTOR_ID;
import static ru.practicum.shareit.testutil.TestConstants.ITEM_REQUEST_ID;
import static ru.practicum.shareit.testutil.TestDataProvider.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemRequestMapper mapper;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void getItemRequestsOfUser_returnsCorrectList() {
        User requestor = getMockUser(ITEM_REQUESTOR_ID);
        ItemRequest request1 = getItemRequest(ITEM_REQUEST_ID, requestor);
        ItemRequest request2 = getItemRequest(ITEM_REQUEST_ID + 10, requestor);
        List<ItemRequest> requests = List.of(request1, request2);
        Mockito
                .when(userRepository.existsById(ITEM_REQUESTOR_ID))
                .thenReturn(true);
        Mockito
                .when(itemRequestRepository.findAllByRequestor_Id(ITEM_REQUESTOR_ID))
                .thenReturn(requests);
        List<ItemRequestResponse> expected = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            ItemRequest request = requests.get(i);
            ItemRequestResponse response = getItemRequestResponse(request, Collections.emptyList());
            expected.add(response);
            Mockito
                    .when(mapper.mapToDto(request))
                    .thenReturn(response);
        }

        List<ItemRequestResponse> actual = itemRequestService.getItemRequestsOfUser(ITEM_REQUESTOR_ID);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getItemRequestsOfUser_whenUserNotExists_throws() {
        Mockito
                .when(userRepository.existsById(ITEM_REQUESTOR_ID))
                .thenReturn(false);
        assertThrows(ServiceException.class,
                () -> itemRequestService.getItemRequestsOfUser(ITEM_REQUESTOR_ID));
    }

}