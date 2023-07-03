package ru.practicum.shareit.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.IdGenerator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@RequiredArgsConstructor
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> userMap = new ConcurrentHashMap<>();
    private final IdGenerator idGenerator;

    @Override
    public Optional<User> findById(long userId) {
        return Optional.ofNullable(userMap.get(userId));
    }

    @Override
    public User save(User user) {
        user.setId(idGenerator.nextId());
        userMap.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> findAll() {
        return List.copyOf(userMap.values());
    }

    @Override
    public User update(Long userId, String email, String name) {
        User user = userMap.get(userId);
        checkUserExists(user, userId);
        updateEmail(user, email);
        updateName(user, name);
        return user;
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

    @Override
    public void deleteById(long userId) {
        userMap.remove(userId);
    }

    @Override
    public boolean isEmailUnique(String email, Long userId) {
        Optional<User> userOpt = userMap.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
        if (userOpt.isEmpty()) { // no User with such email in storage
            return true;
        } else { // there's user with such email
            if (userId == null) { // when creating new User we have no id
                return false;
            }
            User user = userOpt.get();
            // if current user has the same email, we ignore it
            return user.getId().equals(userId);
        }
    }
}
