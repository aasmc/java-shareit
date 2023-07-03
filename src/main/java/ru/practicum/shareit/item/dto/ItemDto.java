package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class ItemDto {
    private Long id;
    private Long ownerId;
    @NotEmpty
    private String name;
    @NotEmpty
    private String description;
    @NotNull
    private Boolean available;
}
