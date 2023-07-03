package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

    Optional<Item> findById(long itemId);

    Item update(Long ownerId,
                Long itemId,
                Boolean available,
                String description,
                String name);

    Item save(Item item);

    List<Item> getItemsForUser(long userId);

    List<Item> searchAvailableItems(String query);

    void deleteItemsForUser(long userId);

    void deleteItem(long userId, long itemId);
}
