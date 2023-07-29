package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
public class ItemRequestDto {
    @NotEmpty
    private String description;
    private Long requestorId;
}
