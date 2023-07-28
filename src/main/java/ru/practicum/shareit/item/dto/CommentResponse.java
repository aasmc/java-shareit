package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.util.DateProcessor;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private String text;
    private String authorName;
    @JsonFormat(pattern = DateProcessor.DATE_FORMAT)
    private LocalDateTime created;
}
