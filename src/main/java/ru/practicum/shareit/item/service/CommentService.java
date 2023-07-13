package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.CommentResponse;

public interface CommentService {

    CommentResponse saveComment(CommentRequest commentRequest);

}
