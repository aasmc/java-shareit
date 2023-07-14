package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemShortDto;

import java.util.List;

public interface ItemService {
    ItemDto findById(Long itemId, Long userId);

    ItemShortDto update(ItemDto patchDto, Long ownerId);

    ItemShortDto saveItem(ItemDto dto);

    List<ItemDto> getItemsForUser(long userId);

    List<ItemShortDto> searchAvailableItems(String query);

    void deleteItemsForUser(long userId);

    void deleteItem(long userId, long itemId);
}
