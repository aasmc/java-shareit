package ru.practicum.shareit.testutil;

import ru.practicum.shareit.user.dto.UserDto;

public class TestDataProvider {

    public static UserDto getUserDto() {
        return UserDto.builder()
                .email("user@user.com")
                .name("user")
                .build();
    }

    public static UserDto expectedUserAfterUpdate() {
        return UserDto.builder()
                .email("update@user.com")
                .name("update")
                .build();
    }

}
