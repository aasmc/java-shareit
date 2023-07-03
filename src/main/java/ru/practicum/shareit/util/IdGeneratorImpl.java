package ru.practicum.shareit.util;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IdGeneratorImpl implements IdGenerator {
    private final AtomicLong currentId = new AtomicLong(0);
    @Override
    public Long nextId() {
        return currentId.incrementAndGet();
    }
}
