package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> createItem(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                              @RequestBody @Valid ItemDto dto) {
        log.info(
                "Received POST request to create Item {} by user with id = {}",
                dto,
                userId
        );
        dto.setOwnerId(userId);
        return ResponseEntity.ok(itemService.saveItem(dto));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                              @RequestBody ItemDto dto,
                                              @PathVariable("itemId") Long itemId) {
        log.info(
                "Received PATCH request to update Item {} by user with id = {}",
                dto,
                userId
        );
        dto.setId(itemId);
        return ResponseEntity.ok(itemService.update(dto, userId));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable("itemId") Long itemId) {
        log.info("Received request to GET Item by id = {}", itemId);
        return ResponseEntity.ok(itemService.findById(itemId));
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getItemsForUser(@RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        log.info("Received request to GET items for user with id={}", userId);
        return ResponseEntity.ok(itemService.getItemsForUser(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(@RequestParam("text") String query) {
        log.info("Received GET request to search for items by query = {}", query);
        return ResponseEntity.ok(itemService.searchAvailableItems(query));
    }

}
