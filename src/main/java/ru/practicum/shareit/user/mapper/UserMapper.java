package ru.practicum.shareit.user.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.Mapper;

@Component
public class UserMapper implements Mapper<User, UserDto, Long> {
    @Override
    public UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    @Override
    public User mapToDomain(UserDto dto, Long domainId) {
        return User.builder()
                .id(domainId)
                .email(dto.getEmail())
                .name(dto.getName())
                .build();
    }
}
