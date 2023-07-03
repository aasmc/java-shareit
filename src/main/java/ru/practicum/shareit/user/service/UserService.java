package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    UserDto getById(long userId);
    UserDto create(UserDto dto);
    List<UserDto> getAllUsers();
    UserDto update(UserDto dto);
    void deleteById(long userId);
}
