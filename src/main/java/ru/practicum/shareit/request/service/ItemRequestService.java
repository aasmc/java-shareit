package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;

import java.util.List;

public interface ItemRequestService {

    ItemRequestResponse addItemRequest(ItemRequestDto dto);

    List<ItemRequestResponse> getItemRequestsOfUser(Long userId);

    List<ItemRequestResponse> getItemRequestsNotOfUser(Long userId, int from, int size);

    ItemRequestResponse getItemRequestById(Long id, Long userId);
}
