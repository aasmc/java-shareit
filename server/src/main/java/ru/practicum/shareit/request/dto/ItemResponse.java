package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemResponse {
    private Long id;
    private String name;
    private String description;
    private boolean available;
    private Long requestId;
}
