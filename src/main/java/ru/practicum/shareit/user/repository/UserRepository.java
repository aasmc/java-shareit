package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(long userId);

    User save(User user);

    List<User> findAll();

    User update(UserDto dto);

    void deleteById(long userId);

    boolean isEmailUnique(String email, Long userId);
}
