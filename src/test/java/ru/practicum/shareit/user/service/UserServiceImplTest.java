package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.testutil.TestDataProvider.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void update_whenUserNotPresent_throws() {
        UserDto dto = getUserDto();
        dto.setId(1L);
        Mockito
                .when(userRepository.findById(dto.getId())).thenReturn(Optional.empty());
        assertThrows(ServiceException.class, () -> userService.update(dto));
    }

    @Test
    void update_whenUserEmailNotUnique_throws() {
        UserDto dto = getUserDto();
        dto.setId(1L);
        dto.setName("New Name");
        dto.setEmail("newEmail@mail.com");
        User user = getMockUser(dto.getId());
        User updated = fromDto(dto);
        Mockito
                .when(userRepository.findById(dto.getId())).thenReturn(Optional.of(user));
        Mockito
                .when(userRepository.saveAndFlush(updated)).thenThrow(DataIntegrityViolationException.class);
        assertThrows(ServiceException.class, () -> userService.update(dto));
    }

    @Test
    void update_whenUserPresentEmailUnique_updatesCorrectly() {
        UserDto dto = getUserDto();
        dto.setId(1L);
        dto.setName("New Name");
        dto.setEmail("newEmail@mail.com");
        User user = getMockUser(dto.getId());
        Mockito
                .when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        User updated = fromDto(dto);
        UserDto updatedDto = fromUser(updated);
        Mockito
                .when(userMapper.mapToDto(updated)).thenReturn(updatedDto);

        UserDto result = userService.update(dto);
        assertThat(result).isEqualTo(updatedDto);
    }

    @Test
    void getAllUsers_whenHasUsers_returnsAllOfThem() {
        List<User> users = new ArrayList<>();
        for (long i = 1; i <= 5; i++) {
            users.add(getMockUser(i));
        }

        Mockito
                .when(userRepository.findAll()).thenReturn(users);
        List<UserDto> dtos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            dtos.add(fromUser(users.get(i)));
        }
        for (int i = 0; i < 5; i++) {
            Mockito
                    .when(userMapper.mapToDto(users.get(i))).thenReturn(dtos.get(i));
        }

        List<UserDto> allUsers = userService.getAllUsers();
        assertThat(allUsers).isEqualTo(dtos);
    }

    @Test
    void getAllUsers_whenNoUsers_returnsEmptyList() {
        Mockito
                .when(userRepository.findAll()).thenReturn(Collections.emptyList());
        List<UserDto> allUsers = userService.getAllUsers();
        assertThat(allUsers).isEmpty();
    }

    @Test
    void getById_whenUserNotExists_throws() {
        long userId = 1L;
        Mockito
                .when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ServiceException.class, () -> userService.getById(userId));
    }

    @Test
    void getById_whenUserExists_returnsUser() {
        Long userId = 1L;
        User user = getMockUser(userId);
        Mockito
                .when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UserDto dto = fromUser(user);
        Mockito
                .when(userMapper.mapToDto(user)).thenReturn(dto);

        UserDto byId = userService.getById(userId);
        assertThat(byId.getId()).isEqualTo(user.getId());
        assertThat(byId.getName()).isEqualTo(user.getName());
        assertThat(byId.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void create_whenEmailNotUnique_throws() {
        UserDto dto = getUserDto();
        User unsaved = getMockUser(null);

        Mockito
                .when(userMapper.mapToDomain(dto)).thenReturn(unsaved);
        Mockito
                .when(userRepository.save(unsaved)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(ServiceException.class, () -> userService.create(dto));
    }

    @Test
    void create_whenEmailUnique_createsNewUser() {
        UserDto dto = getUserDto();
        User unsaved = getMockUser(null);
        User saved = getMockUser(1L);
        UserDto toBeReturned = getUserDto();
        toBeReturned.setId(1L);

        Mockito
                .when(userMapper.mapToDomain(dto)).thenReturn(unsaved);
        Mockito
                .when(userRepository.save(unsaved)).thenReturn(saved);
        Mockito
                .when(userMapper.mapToDto(saved)).thenReturn(toBeReturned);

        UserDto created = userService.create(dto);
        assertThat(created.getId()).isEqualTo(saved.getId());
        assertThat(created.getName()).isEqualTo(saved.getName());
        assertThat(created.getEmail()).isEqualTo(saved.getEmail());
    }

}