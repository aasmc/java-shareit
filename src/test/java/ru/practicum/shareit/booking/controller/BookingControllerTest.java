package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.practicum.shareit.BaseIntegTest;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.booking.model.BookingStatus.APPROVED;
import static ru.practicum.shareit.booking.model.BookingStatus.WAITING;
import static ru.practicum.shareit.testutil.TestConstants.*;
import static ru.practicum.shareit.testutil.TestDataProvider.*;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingControllerTest extends BaseIntegTest {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @SneakyThrows
    @Test
    void getBookingById_returnsBookingForOwner() {
        User booker = saveAndGetUser("booker@email.com");
        User owner = saveAndGetUser("owner@email.com");
        Item item = saveAndGetItemOfUser(owner);
        Booking booking = saveAndGetBooking(booker, item, WAITING);

        mockMvc.perform(
                        get(BOOKINGS_BASE_URL + "/" + booking.getId())
                                .header(USER_HEADER, owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId()))
                .andExpect(jsonPath("$.status").value(booking.getStatus().name()))
                .andExpect(jsonPath("$.booker.id").value(booker.getId()))
                .andExpect(jsonPath("$.item.id").value(item.getId()));
    }


    @Test
    void getBookingById_returnsBookingForBooker() {
        User booker = saveAndGetUser("booker@email.com");
        User owner = saveAndGetUser("owner@email.com");
        Item item = saveAndGetItemOfUser(owner);
        Booking booking = saveAndGetBooking(booker, item, WAITING);

        webTestClient.get().uri(BOOKINGS_BASE_URL + "/" + booking.getId())
                .header(USER_HEADER, booker.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookingResponse.class)
                .value(response -> {
                    assertThat(response.getId()).isEqualTo(booking.getId());
                    assertThat(response.getStatus()).isEqualTo(booking.getStatus());
                    assertThat(response.getStart()).isEqualTo(booking.getStart());
                    assertThat(response.getEnd()).isEqualTo(booking.getEnd());
                    assertThat(response.getBooker().getId()).isEqualTo(booker.getId());
                    assertThat(response.getItem().getId()).isEqualTo(item.getId());
                });
    }

    @Test
    void updateStatus_updatesStatusOfBooking() {
        User booker = saveAndGetUser("booker@email.com");
        User owner = saveAndGetUser("owner@email.com");
        Item item = saveAndGetItemOfUser(owner);
        Booking booking = saveAndGetBooking(booker, item, WAITING);

        webTestClient.patch().uri(BOOKINGS_BASE_URL + "/" + booking.getId() + "?approved=true")
                .header(USER_HEADER, owner.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookingResponse.class)
                .value(response -> {
                    assertThat(response.getId()).isEqualTo(booking.getId());
                    assertThat(response.getStatus()).isEqualTo(APPROVED);
                });
    }

    @Test
    void createBooking_whenAllCorrect_createsBooking() {
        User savedBooker = saveAndGetUser("booker@email.com");
        User savedOwner = saveAndGetUser("owner@email.com");
        Item savedItem = saveAndGetItemOfUser(savedOwner);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(10);
        BookingRequest dto = getBookingRequestForCreate(start, end, savedItem.getId());

        webTestClient.post().uri(BOOKINGS_BASE_URL)
                .header(USER_HEADER, savedBooker.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(BookingResponse.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getStart()).isEqualTo(dto.getStart());
                    assertThat(response.getEnd()).isEqualTo(dto.getEnd());
                    assertThat(response.getStatus()).isEqualTo(WAITING);
                    assertThat(response.getItem().getId()).isEqualTo(savedItem.getId());
                    assertThat(response.getBooker().getId()).isEqualTo(savedBooker.getId());
                });
    }

    @Test
    void createBooking_whenEndBeforeStart_returns400() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().minusDays(1);
        BookingRequest dto = getBookingRequestForCreate(start, end, ITEM_ID);

        webTestClient.post().uri(BOOKINGS_BASE_URL)
                .header(USER_HEADER, BOOKER_ID.toString())
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createBooking_whenStartInPast_returns400() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequest dto = getBookingRequestForCreate(start, end, ITEM_ID);

        webTestClient.post().uri(BOOKINGS_BASE_URL)
                .header(USER_HEADER, BOOKER_ID.toString())
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    private Booking saveAndGetBooking(User booker, Item item, BookingStatus status) {
        Booking booking = getBookingBeforeSave(booker, item);
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    private Item saveAndGetItemOfUser(User owner) {
        Item item = getTransientAvailableItemNoBookingsNoRequest(owner);
        return itemRepository.save(item);
    }

    private User saveAndGetUser(String email) {
        User user = getMockUser(null);
        user.setEmail(email);
        return userRepository.save(user);
    }


}