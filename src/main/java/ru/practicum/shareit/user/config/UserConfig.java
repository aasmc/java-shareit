package ru.practicum.shareit.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.shareit.user.model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class UserConfig {
    @Bean
    public Map<Long, User> userMap() {
        return new ConcurrentHashMap<>();
    }
}
