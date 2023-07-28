package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.CommentResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CommentService {

    CommentResponse saveComment(CommentRequest commentRequest);

    List<CommentResponse> getCommentsOfItem(Long itemId);

    Map<Long, List<CommentResponse>> getItemIdToComments(Set<Long> itemIds);
}
