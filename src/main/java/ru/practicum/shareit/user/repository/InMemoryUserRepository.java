package ru.practicum.shareit.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.IdGenerator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class InMemoryUserRepository {

    private final Map<Long, User> userMap = new ConcurrentHashMap<>();
    private final IdGenerator idGenerator;

    public Optional<User> findById(long userId) {
        return Optional.ofNullable(userMap.get(userId));
    }

    public User save(User user) {
        user.setId(idGenerator.nextId());
        userMap.put(user.getId(), user);
        return user;
    }

    public List<User> findAll() {
        return List.copyOf(userMap.values());
    }

    public User update(UserDto dto) {
        User user = userMap.get(dto.getId());
        checkUserExists(user, dto.getId());
        updateEmail(user, dto.getEmail());
        updateName(user, dto.getName());
        return user;
    }

    public void deleteById(long userId) {
        userMap.remove(userId);
    }

    public boolean isEmailUnique(String email, Long userId) {
        Optional<User> userOpt = userMap.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
        if (userOpt.isEmpty()) { // no User with such email in storage
            return true;
        } else { // there's user with such email
            if (userId == null) { // we are creating new user with email which is already present
                return false;
            }
            // we are updating an existing user, check if the user we are updating
            // is the one with the same email. If so, ignore it.
            User user = userOpt.get();
            return user.getId().equals(userId);
        }
    }

    private void updateName(User user, String name) {
        if (null != name) {
            user.setName(name);
        }
    }

    private void updateEmail(User user, String email) {
        if (null != email) {
            user.setEmail(email);
        }
    }

    private void checkUserExists(User user, Long userId) {
        if (null == user) {
            String msg = String.format("User with ID=%d not found.", userId);
            throw new ServiceException(HttpStatus.NOT_FOUND.value(), msg);
        }
    }
}
