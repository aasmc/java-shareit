package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable("userId") long userId,
                                              @RequestBody @Valid UserDto dto) {
        log.info("Received PATCH request to update user with id={}. User to update={}",
                userId,
                dto);
        dto.setId(userId);
        return ResponseEntity.ok(userService.update(dto));
    }

    @DeleteMapping("/{userId}")
    public void deleteById(@PathVariable("userId") long userId) {
        log.info("Received request to DELETE user by id={}", userId);
        userService.deleteById(userId);
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid UserDto dto) {
        log.info("Received request to POST user={}", dto);
        return ResponseEntity.ok(userService.create(dto));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("userId") long userId) {
        log.info("Received request to GET user by id={}", userId);
        return ResponseEntity.ok(userService.getById(userId));
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAll() {
        log.info("Received request to GET all users.");
        return ResponseEntity.ok(userService.getAllUsers());
    }

}
