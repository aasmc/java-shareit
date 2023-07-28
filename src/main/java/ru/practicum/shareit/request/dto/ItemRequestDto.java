package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
@Data
@Builder
public class ItemRequestDto {
    @NotEmpty
    private String description;
    private Long requestorId;
}
