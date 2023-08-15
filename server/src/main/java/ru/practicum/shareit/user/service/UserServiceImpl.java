package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    @Override
    public UserDto getById(long userId) {
        User user = findByIdOrThrow(userId);
        return userMapper.mapToDto(user);
    }

    @Override
    public UserDto create(UserDto dto) {
        try {
            User saved = userRepository.save(userMapper.mapToDomain(dto));
            return userMapper.mapToDto(saved);
        } catch (DataIntegrityViolationException ex) {
            String msg = "You have provided email that already exists. Please create new one.";
            throw new ServiceException(HttpStatus.CONFLICT.value(), msg);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto update(UserDto dto) {
        User toUpdate = findByIdOrThrow(dto.getId());
        try {
            updateEmail(dto, toUpdate);
            updateName(dto, toUpdate);
            userRepository.saveAndFlush(toUpdate);
            return userMapper.mapToDto(toUpdate);
        } catch (DataIntegrityViolationException ex) {
            String msg = "You have provided email that already exists. Please create new one.";
            throw new ServiceException(HttpStatus.CONFLICT.value(), msg);
        }
    }

    @Override
    public void deleteById(long userId) {
        userRepository.deleteById(userId);
    }

    private User findByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    String msg = String.format("User with ID=%d not found", userId);
                    return new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
                });
    }

    private void updateName(UserDto dto, User toUpdate) {
        if (dto.getName() != null) {
            toUpdate.setName(dto.getName());
        }
    }

    private void updateEmail(UserDto dto, User toUpdate) {
        if (dto.getEmail() != null) {
            toUpdate.setEmail(dto.getEmail());
        }
    }

}
