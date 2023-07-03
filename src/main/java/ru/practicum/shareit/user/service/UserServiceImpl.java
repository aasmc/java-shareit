package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto getById(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    String msg = String.format("User with ID=%d not found.", userId);
                    return new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
                });
        return userMapper.mapToDto(user);
    }

    @Override
    public UserDto create(UserDto dto) {
        validateEmail(dto);
        User saved = userRepository.save(userMapper.mapToDomain(dto));
        return userMapper.mapToDto(saved);
    }

    private void validateEmail(UserDto dto) {
        if (dto.getEmail() == null) {
            String msg = "User email cannot be null";
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), msg);
        }
        validateEmailUnique(dto.getEmail(), dto.getId());
    }

    private void validateEmailUnique(String email, Long userId) {
        if (email == null) return;
        if (!isEmailUnique(email, userId)) {
            String msg = "You have provided email that already exists. Please create new one.";
            throw new ServiceException(HttpStatus.CONFLICT.value(), msg);
        }
    }

    private boolean isEmailUnique(String email, Long userId) {
        return userRepository.isEmailUnique(email, userId);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto update(UserDto dto) {
        validateEmailUnique(dto.getEmail(), dto.getId());
        User updated = userRepository.update(dto.getId(), dto.getEmail(), dto.getName());
        return userMapper.mapToDto(updated);
    }

    @Override
    public void deleteById(long userId) {
        userRepository.deleteById(userId);
    }
}
