package ru.practicum.shareit.gateway.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.gateway.client.BaseClient;
import ru.practicum.shareit.gateway.user.dto.CreateUserDto;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> updateUser(long userId, CreateUserDto dto) {
        String path = String.format("/%d", userId);
        return patch(path, dto);
    }

    public ResponseEntity<Object> deleteById(long userId) {
        String path = String.format("/%d", userId);
        return delete(path);
    }

    public ResponseEntity<Object> createUser(CreateUserDto dto) {
        return post("", dto);
    }

    public ResponseEntity<Object> getUserById(long userId) {
        String path = String.format("/%d", userId);
        return get(path);
    }

    public ResponseEntity<Object> getAll() {
        return get("");
    }
}
