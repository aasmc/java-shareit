package ru.practicum.shareit.item.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemMapper implements Mapper<Item, ItemDto, ItemDto> {

    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    public ItemDto mapToDto(Item item) {
        List<Comment> comments = item.getComments();
        List<CommentResponse> commentResponses = null;
        if (comments != null) {
            commentResponses = comments.stream()
                    .map(commentMapper::mapToDto)
                    .collect(Collectors.toList());
        }
        return ItemDto.builder()
                .ownerId(item.getOwner().getId())
                .name(item.getName())
                .description(item.getDescription())
                .id(item.getId())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .comments(commentResponses)
                .build();
    }

    @Override
    public Item mapToDomain(ItemDto itemDto) {
        User owner = userRepository.findById(itemDto.getOwnerId())
                .orElseThrow(() -> {
                    String msg = String.format("User with ID=%d not found.", itemDto.getOwnerId());
                    return new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
                });

        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .owner(owner)
                .available(itemDto.getAvailable())
                .request(null) // TODO fix, after implementing ItemRequest logic
                .build();
    }
}
