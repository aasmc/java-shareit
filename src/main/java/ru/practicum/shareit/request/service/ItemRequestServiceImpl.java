package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.OffsetBasedPageRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRequestMapper mapper;
    private final UserRepository userRepository;

    @Override
    public ItemRequestResponse addItemRequest(ItemRequestDto dto) {
        ItemRequest request = mapper.mapToDomain(dto);
        request = itemRequestRepository.save(request);
        return mapper.mapToDto(request);
    }

    @Override
    public List<ItemRequestResponse> getItemRequestsOfUser(Long userId) {
        checkUserExists(userId);
        return itemRequestRepository.findAllByRequestor_Id(userId)
                .stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestResponse> getItemRequestsNotOfUser(Long userId, int from, int size) {
        Pageable pageable = new OffsetBasedPageRequest(from,
                size,
                Sort.by(Sort.Direction.DESC, "created"));
        return itemRequestRepository.findAllNotOfUser(userId, pageable)
                .stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestResponse getItemRequestById(Long id, Long userid) {
        checkUserExists(userid);
        ItemRequest request = itemRequestRepository.findById(id)
                .orElseThrow(() -> {
                    String msg = String.format("ItemRequest with ID=%d not found", id);
                    return new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
                });
        return mapper.mapToDto(request);
    }

    private void checkUserExists(Long userId) {
        boolean exists = userRepository.existsById(userId);
        if (!exists) {
            String msg = String.format("User with ID=%d not found.", userId);
            throw new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
        }
    }
}
