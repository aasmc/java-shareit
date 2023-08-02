package ru.practicum.shareit.item.dto;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CommentResponseTest {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final JacksonTester<CommentResponse> tester;

    @SneakyThrows
    @Test
    void testSerialize() {
        LocalDateTime now = LocalDateTime.of(
                LocalDate.of(2023, Month.JULY, 29),
                LocalTime.of(12, 0)
        );
        String expected = DateTimeFormatter.ofPattern(DATE_FORMAT).format(now);
        CommentResponse dto = CommentResponse.builder()
                .id(1L)
                .text("text")
                .authorName("authorname")
                .created(now)
                .build();

        JsonContent<CommentResponse> content = tester.write(dto);
        assertThat(content).extractingJsonPathStringValue("$.created").isEqualTo(expected);
        assertThat(content).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(content).extractingJsonPathStringValue("$.text").isEqualTo(dto.getText());
        assertThat(content).extractingJsonPathStringValue("$.authorName").isEqualTo(dto.getAuthorName());
    }

}