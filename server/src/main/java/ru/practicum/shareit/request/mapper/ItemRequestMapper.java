package ru.practicum.shareit.request.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.dto.ItemResponse;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemRequestMapper implements Mapper<ItemRequest, ItemRequestDto, ItemRequestResponse> {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestResponse mapToDto(ItemRequest domain) {
        List<ItemResponse> items = itemRepository.findAllByRequest_Id(domain.getId())
                .stream()
                .map(i -> mapItem(i, domain.getId()))
                .collect(Collectors.toList());
        return ItemRequestResponse.builder()
                .id(domain.getId())
                .description(domain.getDescription())
                .created(domain.getCreated())
                .items(items)
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
                .description(dto.getDescription())
                .requestor(requestor)
                .build();
    }

    private ItemResponse mapItem(Item item, long requestId) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .available(item.getAvailable())
                .description(item.getDescription())
                .requestId(requestId)
                .build();
    }
}
