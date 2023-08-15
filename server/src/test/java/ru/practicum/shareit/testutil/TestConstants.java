package ru.practicum.shareit.testutil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;

public class TestConstants {

    public static final String USERS_BASE_URL = "/users";
    public static final String ITEMS_BASE_URL = "/items";
    public static final String BOOKINGS_BASE_URL = "/bookings";
    public static final String REQUESTS_BASE_URL = "/requests";
    public static final String USER_HEADER = "X-Sharer-User-Id";
    public static final Long BOOKER_ID = 1L;
    public static final Long BOOKING_ID = 2L;
    public static final Long ITEM_ID = 3L;
    public static final Long OWNER_ID = 4L;
    public static final Long ITEM_REQUESTOR_ID = 5L;
    public static final Long ITEM_REQUEST_ID = 6L;

    public static final LocalDateTime BOOKING_START = LocalDateTime.now().minusDays(1);

    public static final LocalDateTime BOOKING_END = BOOKING_START.plusDays(1);

    public static final LocalDateTime BOOKING_REQUEST_START = LocalDateTime.of(
            LocalDate.of(2023, Month.JULY, 30),
            LocalTime.of(12, 0)
    );

    public static final LocalDateTime BOOKING_REQUEST_END = LocalDateTime.of(
            LocalDate.of(2023, Month.JULY, 31),
            LocalTime.of(12, 0)
    );


    public static final LocalDateTime ITEM_REQUEST_CREATED = LocalDateTime.of(
            LocalDate.of(2023, Month.JULY, 25),
            LocalTime.of(12, 0)
    );

}
