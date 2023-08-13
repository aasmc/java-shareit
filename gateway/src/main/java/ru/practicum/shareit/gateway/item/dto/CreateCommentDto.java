package ru.practicum.shareit.gateway.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentDto {
    @NotEmpty
    private String text;
}
