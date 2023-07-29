package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto findById(Long itemId, Long userId);

    ItemDto update(ItemDto patchDto, Long ownerId);

    ItemDto saveItem(ItemDto dto);

    List<ItemDto> getItemsForUser(long userId, int from, int size);

    List<ItemDto> searchAvailableItems(String query, int from, int size);

    void deleteItemsForUser(long userId);

    void deleteItem(long userId, long itemId);
}
