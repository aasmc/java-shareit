package ru.practicum.shareit.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class IdGeneratorImplTest {

    IdGenerator generator;

    @BeforeEach
    void setup() {
        generator = new IdGeneratorImpl();
    }

    @Test
    void testNextId() {
        Long id = generator.nextId();
        assertThat(id).isEqualTo(1L);

        id = generator.nextId();
        assertThat(id).isEqualTo(2L);
    }

}