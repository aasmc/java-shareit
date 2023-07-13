package ru.practicum.shareit.request.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.Mapper;

@Component
@RequiredArgsConstructor
public class ItemRequestMapper implements Mapper<ItemRequest, ItemRequestDto> {

    private final UserRepository userRepository;

    @Override
    public ItemRequestDto mapToDto(ItemRequest domain) {
        return ItemRequestDto.builder()
                .id(domain.getId())
                .description(domain.getDescription())
                .requestorId(domain.getRequestor().getId())
                .created(domain.getCreated())
                .build();
    }

    @Override
    public ItemRequest mapToDomain(ItemRequestDto dto) {
        User requestor = userRepository.findById(dto.getRequestorId())
                .orElseThrow(() -> {
                    String msg = String.format(
                            "Item Requestor with ID=%d not found.",
                            dto.getRequestorId()
                    );
                    return new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
                });
        return ItemRequest.builder()
                .id(dto.getId())
                .description(dto.getDescription())
                .created(dto.getCreated())
                .requestor(requestor)
                .build();
    }
}
