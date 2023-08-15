package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ItemRequestResponse {
    private Long id;
    private LocalDateTime created;
    private String description;
    private List<ItemResponse> items;
}
