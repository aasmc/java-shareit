package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

    Optional<Item> findById(long itemId);

    Item update(Long ownerId, ItemDto patchDto);

    Item save(Item item);

    List<Item> getItemsForUser(long userId);

    List<Item> searchAvailableItems(String query);

    void deleteItemsForUser(long userId);

    void deleteItem(long userId, long itemId);
}
