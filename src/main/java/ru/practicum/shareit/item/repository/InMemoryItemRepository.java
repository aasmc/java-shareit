package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.util.IdGenerator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Map<Long, Item>> userToItemsMap = new ConcurrentHashMap<>();
    private final Map<Long, Item> itemMap = new ConcurrentHashMap<>();
    private final IdGenerator idGenerator;

    @Override
    public Optional<Item> findById(long itemId) {
        return Optional.ofNullable(itemMap.get(itemId));
    }

    @Override
    public Item update(Long ownerId,
                       Long itemId,
                       Boolean available,
                       String description,
                       String name) {
        Map<Long, Item> userItems = userToItemsMap.get(ownerId);
        checkItemBelongsToUser(userItems, itemId, ownerId);
        Item toUpdate = userItems.get(itemId);
        updateAvailable(toUpdate, available);
        updateDescription(toUpdate, description);
        updateName(toUpdate, name);
        return toUpdate;
    }

    private void updateName(Item toUpdate, String name) {
        if (name != null) {
            toUpdate.setName(name);
        }
    }

    private void updateDescription(Item toUpdate, String description) {
        if (description != null) {
            toUpdate.setDescription(description);
        }
    }

    private void updateAvailable(Item toUpdate,
                                 Boolean available) {
        if (available != null) {
            toUpdate.setAvailable(available);
        }
    }

    private void checkItemBelongsToUser(Map<Long, Item> userItems, Long itemId, Long ownerId) {
        if (userItems == null || !userItems.containsKey(itemId)) {
            String msg = String.format(
                    "Cannot update Item with id=%d because it doesn't belong to user with id=%d",
                    itemId,
                    ownerId
            );
            throw new ServiceException(HttpStatus.FORBIDDEN.value(), msg);
        }
    }

    @Override
    public Item save(Item item) {
        item.setId(idGenerator.nextId());
        itemMap.put(item.getId(), item);
        Map<Long, Item> userItems = userToItemsMap.computeIfAbsent(
                item.getOwner().getId(),
                k -> new ConcurrentHashMap<>()
        );
        userItems.put(item.getId(), item);
        return item;
    }

    @Override
    public List<Item> getItemsForUser(long userId) {
        return List.copyOf(userToItemsMap.get(userId).values());
    }

    @Override
    public List<Item> searchAvailableItems(String query) {
        return itemMap.values().stream()
                .filter(Item::getAvailable)
                .filter(i -> i.getName().toLowerCase().contains(query.toLowerCase())
                        || i.getDescription().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItemsForUser(long userId) {
        userToItemsMap.remove(userId);
        itemMap.values().removeIf(i -> i.getOwner().getId().equals(userId));
    }

    @Override
    public void deleteItem(long userId, long itemId) {
        Map<Long, Item> userItems = userToItemsMap.get(userId);
        if (userItems != null) {
            userItems.remove(itemId);
        } else {
            String msg = String.format(
                    "Cannot delete item with id=%d because it doesn't belong to user with id=%d",
                    itemId,
                    userId
            );
            throw new ServiceException(HttpStatus.FORBIDDEN.value(), msg);
        }
        itemMap.remove(itemId);
    }
}