package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto findById(long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    String msg = String.format("Item with id=%d not found.", itemId);
                    return new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
                });
        return itemMapper.mapToDto(item);
    }

    @Override
    public ItemDto update(ItemDto patchDto, Long ownerId) {
        Item updated = itemRepository.update(ownerId, patchDto);
        return itemMapper.mapToDto(updated);
    }

    @Override
    public ItemDto saveItem(ItemDto dto) {
        Item item = itemMapper.mapToDomain(dto);
        Item saved = itemRepository.save(item);
        return itemMapper.mapToDto(saved);
    }

    @Override
    public List<ItemDto> getItemsForUser(long userId) {
        return itemRepository.getItemsForUser(userId)
                .stream()
                .map(itemMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchAvailableItems(String query) {
        if (ObjectUtils.isEmpty(query)) {
            return Collections.emptyList();
        }
        return itemRepository.searchAvailableItems(query)
                .stream()
                .map(itemMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItemsForUser(long userId) {
        itemRepository.deleteItemsForUser(userId);
    }

    @Override
    public void deleteItem(long userId, long itemId) {
        itemRepository.deleteItem(userId, itemId);
    }
}
