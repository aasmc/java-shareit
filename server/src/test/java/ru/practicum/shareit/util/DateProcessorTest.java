package ru.practicum.shareit.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DateProcessorTest {

    @Test
    void testToString() {
        LocalDateTime date = LocalDateTime.of(2023, Month.AUGUST, 1, 12, 12, 12);
        String expected = "2023-08-01 12:12:12";
        String actual = DateProcessor.toString(date);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testToDate() {
        String dateStr = "2023-08-01 12:12:12";
        LocalDateTime date = DateProcessor.toDate(dateStr);

        assertThat(date.getYear()).isEqualTo(2023);
        assertThat(date.getMonth()).isEqualTo(Month.AUGUST);
        assertThat(date.getDayOfMonth()).isEqualTo(1);
    }

}