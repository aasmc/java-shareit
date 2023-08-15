package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@Slf4j
@RequiredArgsConstructor
public class ItemRequestController {

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestResponse addRequest(@RequestHeader(value = USER_HEADER) Long userId,
                                          @RequestBody ItemRequestDto dto) {
        dto.setRequestorId(userId);
        log.info("Received POST request to create ItemRequest {}", dto);
        return itemRequestService.addItemRequest(dto);
    }

    @GetMapping
    public List<ItemRequestResponse> getRequestsOfUser(@RequestHeader(value = USER_HEADER) Long userId) {
        log.info("Received request to GET all ItemRequests of user with id={}", userId);
        return itemRequestService.getItemRequestsOfUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponse> getAllRequests(@RequestHeader(value = USER_HEADER) Long userId,
                                                    @RequestParam(value = "from", defaultValue = "0", required = false) int from,
                                                    @RequestParam(value = "size", defaultValue = "10", required = false) int size) {
        log.info("Received request to GET all ItemRequests not belonging to user with id={}", userId);
        return itemRequestService.getItemRequestsNotOfUser(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponse getRequest(@RequestHeader(value = USER_HEADER) Long userId,
                                          @PathVariable("requestId") Long id) {
        log.info("Received request to GET ItemRequest with id={}", id);
        return itemRequestService.getItemRequestById(id, userId);
    }
}
