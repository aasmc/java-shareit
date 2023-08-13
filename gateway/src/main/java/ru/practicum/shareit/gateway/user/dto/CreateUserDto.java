package ru.practicum.shareit.gateway.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateUserDto {
    private Long id;
    private String name;
    @Email
    @NotNull
    private String email;
}
