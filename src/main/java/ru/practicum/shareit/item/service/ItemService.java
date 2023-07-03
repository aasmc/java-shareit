package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto findById(long itemId);

    ItemDto update(ItemDto patchDto, Long ownerId);

    ItemDto saveItem(ItemDto dto);

    List<ItemDto> getItemsForUser(long userId);

    List<ItemDto> searchAvailableItems(String query);

    void deleteItemsForUser(long userId);

    void deleteItem(long userId, long itemId);
}
