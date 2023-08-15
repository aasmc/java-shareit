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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.booking.model.BookingStatus.*;
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
        Booking booking = saveAndGetBooking(booker, item, WAITING, BOOKING_START, BOOKING_END);

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
        Booking booking = saveAndGetBooking(booker, item, WAITING, BOOKING_START, BOOKING_END);

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
    void getAllBookingsOfOwner_stateREJECTED_whenBookings_returnsCorrectList() {
        User owner = saveAndGetUser("owner@email.com");
        User owner2 = saveAndGetUser("owner2@email.com");
        User booker = saveAndGetUser("booker@email.com");
        Item one = saveAndGetItemOfUser(owner);
        Item two = saveAndGetItemOfUser(owner2); // other owner
        Item three = saveAndGetItemOfUser(owner);
        Item four = saveAndGetItemOfUser(owner);
        Booking booking = saveAndGetBooking(booker, one, REJECTED, BOOKING_START, BOOKING_END);
        saveAndGetBooking(booker, three, WAITING, BOOKING_START.plusDays(2), BOOKING_END.plusDays(2));
        Booking booking2 = saveAndGetBooking(booker, four, REJECTED, BOOKING_START.plusDays(3), BOOKING_END.plusDays(3));
        // this booking is for item of other owner, and will not be included in the result list
        saveAndGetBooking(booker, two, APPROVED, BOOKING_START.plusDays(1), BOOKING_END.plusDays(1));

        webTestClient.get().uri(BOOKINGS_BASE_URL + "/owner?state=REJECTED&from=0&size=10")
                .header(USER_HEADER, owner.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookingResponse.class)
                .value(responses -> {
                    assertThat(responses).hasSize(2);
                    assertThat(responses.get(0).getId()).isEqualTo(booking2.getId());
                    assertThat(responses.get(1).getId()).isEqualTo(booking.getId());
                });
    }

    @Test
    void getAllBookingsOfOwner_stateWAITING_whenBookings_returnsCorrectList() {
        User owner = saveAndGetUser("owner@email.com");
        User owner2 = saveAndGetUser("owner2@email.com");
        User booker = saveAndGetUser("booker@email.com");
        Item one = saveAndGetItemOfUser(owner);
        Item two = saveAndGetItemOfUser(owner2); // other owner
        Item three = saveAndGetItemOfUser(owner);
        Item four = saveAndGetItemOfUser(owner);
        Booking booking = saveAndGetBooking(booker, one, WAITING, BOOKING_START, BOOKING_END);
        saveAndGetBooking(booker, three, REJECTED, BOOKING_START.plusDays(2), BOOKING_END.plusDays(2));
        Booking booking2 = saveAndGetBooking(booker, four, WAITING, BOOKING_START.plusDays(3), BOOKING_END.plusDays(3));
        // this booking is for item of other owner, and will not be included in the result list
        saveAndGetBooking(booker, two, APPROVED, BOOKING_START.plusDays(1), BOOKING_END.plusDays(1));

        webTestClient.get().uri(BOOKINGS_BASE_URL + "/owner?state=WAITING&from=0&size=10")
                .header(USER_HEADER, owner.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookingResponse.class)
                .value(responses -> {
                    assertThat(responses).hasSize(2);
                    assertThat(responses.get(0).getId()).isEqualTo(booking2.getId());
                    assertThat(responses.get(1).getId()).isEqualTo(booking.getId());
                });
    }

    @Test
    void getAllBookingsOfOwner_stateFUTURE_whenBookings_returnsCorrectList() {
        User owner = saveAndGetUser("owner@email.com");
        User owner2 = saveAndGetUser("owner2@email.com");
        User booker = saveAndGetUser("booker@email.com");
        Item one = saveAndGetItemOfUser(owner);
        Item two = saveAndGetItemOfUser(owner2); // other owner
        Item three = saveAndGetItemOfUser(owner);
        Item four = saveAndGetItemOfUser(owner);
        LocalDateTime startOne = LocalDateTime.now().minusDays(1);
        LocalDateTime endOne = LocalDateTime.now().plusDays(2);
        // CURRENT
        saveAndGetBooking(booker, one, WAITING, startOne, endOne);
        // FUTURE
        Booking booking = saveAndGetBooking(booker, three, REJECTED, startOne.plusDays(2), endOne.plusDays(2));
        // PAST
        saveAndGetBooking(booker, four, APPROVED, startOne.minusDays(3), endOne.minusDays(3));
        // this booking is for item of other owner, and will not be included in the result list
        saveAndGetBooking(booker, two, APPROVED, BOOKING_START.plusDays(1), BOOKING_END.plusDays(1));

        webTestClient.get().uri(BOOKINGS_BASE_URL + "/owner?state=FUTURE&from=0&size=10")
                .header(USER_HEADER, owner.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookingResponse.class)
                .value(responses -> {
                    assertThat(responses).hasSize(1);
                    assertThat(responses.get(0).getId()).isEqualTo(booking.getId());
                });
    }

    @Test
    void getAllBookingsOfOwner_statePAST_whenBookings_returnsCorrectList() {
        User owner = saveAndGetUser("owner@email.com");
        User owner2 = saveAndGetUser("owner2@email.com");
        User booker = saveAndGetUser("booker@email.com");
        Item one = saveAndGetItemOfUser(owner);
        Item two = saveAndGetItemOfUser(owner2); // other owner
        Item three = saveAndGetItemOfUser(owner);
        Item four = saveAndGetItemOfUser(owner);
        LocalDateTime startOne = LocalDateTime.now().minusDays(1);
        LocalDateTime endOne = LocalDateTime.now().plusDays(2);
        // CURRENT
        saveAndGetBooking(booker, one, WAITING, startOne, endOne);
        // FUTURE
        saveAndGetBooking(booker, three, REJECTED, startOne.plusDays(2), endOne.plusDays(2));
        // PAST
        Booking booking = saveAndGetBooking(booker, four, APPROVED, startOne.minusDays(3), endOne.minusDays(3));
        // this booking is for item of other owner, and will not be included in the result list
        saveAndGetBooking(booker, two, APPROVED, BOOKING_START.plusDays(1), BOOKING_END.plusDays(1));

        webTestClient.get().uri(BOOKINGS_BASE_URL + "/owner?state=PAST&from=0&size=10")
                .header(USER_HEADER, owner.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookingResponse.class)
                .value(responses -> {
                    assertThat(responses).hasSize(1);
                    assertThat(responses.get(0).getId()).isEqualTo(booking.getId());
                });
    }

    @Test
    void getAllBookingsOfOwner_stateCURRENT_whenBookings_returnsCorrectList() {
        User owner = saveAndGetUser("owner@email.com");
        User owner2 = saveAndGetUser("owner2@email.com");
        User booker = saveAndGetUser("booker@email.com");
        Item one = saveAndGetItemOfUser(owner);
        Item two = saveAndGetItemOfUser(owner2); // other owner
        Item three = saveAndGetItemOfUser(owner);
        Item four = saveAndGetItemOfUser(owner);
        LocalDateTime startOne = LocalDateTime.now().minusDays(1);
        LocalDateTime endOne = LocalDateTime.now().plusDays(2);
        // CURRENT
        Booking booking = saveAndGetBooking(booker, one, WAITING, startOne, endOne);
        // FUTURE
        saveAndGetBooking(booker, three, REJECTED, startOne.plusDays(2), endOne.plusDays(2));
        // PAST
        saveAndGetBooking(booker, four, APPROVED, startOne.minusDays(3), endOne.minusDays(3));
        // this booking is for item of other owner, and will not be included in the result list
        saveAndGetBooking(booker, two, APPROVED, BOOKING_START.plusDays(1), BOOKING_END.plusDays(1));

        webTestClient.get().uri(BOOKINGS_BASE_URL + "/owner?state=CURRENT&from=0&size=10")
                .header(USER_HEADER, owner.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookingResponse.class)
                .value(responses -> {
                    assertThat(responses).hasSize(1);
                    assertThat(responses.get(0).getId()).isEqualTo(booking.getId());
                });
    }

    @Test
    void getAllBookingsOfOwner_stateALL_whenBookings_returnsCorrectList() {
        User owner = saveAndGetUser("owner@email.com");
        User owner2 = saveAndGetUser("owner2@email.com");
        User booker = saveAndGetUser("booker@email.com");
        Item one = saveAndGetItemOfUser(owner);
        Item two = saveAndGetItemOfUser(owner2); // other owner
        Item three = saveAndGetItemOfUser(owner);
        Item four = saveAndGetItemOfUser(owner);
        Booking booking = saveAndGetBooking(booker, one, WAITING, BOOKING_START, BOOKING_END);
        Booking booking2 = saveAndGetBooking(booker, three, REJECTED, BOOKING_START.plusDays(2), BOOKING_END.plusDays(2));
        Booking booking3 = saveAndGetBooking(booker, four, APPROVED, BOOKING_START.plusDays(3), BOOKING_END.plusDays(3));
        // this booking is for item of other owner, and will not be included in the result list
        saveAndGetBooking(booker, two, APPROVED, BOOKING_START.plusDays(1), BOOKING_END.plusDays(1));

        webTestClient.get().uri(BOOKINGS_BASE_URL + "/owner?state=ALL&from=0&size=10")
                .header(USER_HEADER, owner.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookingResponse.class)
                .value(responses -> {
                    assertThat(responses).hasSize(3);
                    assertThat(responses.get(0).getId()).isEqualTo(booking3.getId());
                    assertThat(responses.get(1).getId()).isEqualTo(booking2.getId());
                    assertThat(responses.get(2).getId()).isEqualTo(booking.getId());
                });
    }

    @Test
    void getAllBookingsOfOwner_whenHasNoBookings_returnsEmptyList() {
        User owner = saveAndGetUser("owner@email.com");
        saveAndGetItemOfUser(owner);

        webTestClient.get().uri(BOOKINGS_BASE_URL + "/owner?state=ALL&from=0&size=10")
                .header(USER_HEADER, owner.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookingResponse.class)
                .value(responses -> assertThat(responses).isEmpty());
    }

    @Test
    void getBookingsOfUser_stateREJECTED_whenHasBookings_returnsCorrectList() {
        User owner = saveAndGetUser("owner@email.com");
        User booker = saveAndGetUser("booker@email.com");
        Item one = saveAndGetItemOfUser(owner);
        Item two = saveAndGetItemOfUser(owner);
        Item three = saveAndGetItemOfUser(owner);
        Booking booking = saveAndGetBooking(booker, one, REJECTED, BOOKING_START, BOOKING_END);
        saveAndGetBooking(booker, two, APPROVED, BOOKING_START.plusDays(1), BOOKING_END.plusDays(1));
        Booking booking3 = saveAndGetBooking(booker, three, REJECTED, BOOKING_START.plusDays(2), BOOKING_END.plusDays(2));

        webTestClient.get().uri(BOOKINGS_BASE_URL + "?state=REJECTED&from=0&size=10")
                .header(USER_HEADER, booker.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookingResponse.class)
                .value(responses -> {
                    assertThat(responses).hasSize(2);
                    // sorted by start descending
                    assertThat(responses.get(0).getId()).isEqualTo(booking3.getId());
                    assertThat(responses.get(1).getId()).isEqualTo(booking.getId());
                });
    }

    @Test
    void getBookingsOfUser_stateWAITING_whenHasBookings_returnsCorrectList() {
        User owner = saveAndGetUser("owner@email.com");
        User booker = saveAndGetUser("booker@email.com");
        Item one = saveAndGetItemOfUser(owner);
        Item two = saveAndGetItemOfUser(owner);
        Item three = saveAndGetItemOfUser(owner);
        Booking booking = saveAndGetBooking(booker, one, WAITING, BOOKING_START, BOOKING_END);
        saveAndGetBooking(booker, two, APPROVED, BOOKING_START.plusDays(1), BOOKING_END.plusDays(1));
        Booking booking3 = saveAndGetBooking(booker, three, WAITING, BOOKING_START.plusDays(2), BOOKING_END.plusDays(2));

        webTestClient.get().uri(BOOKINGS_BASE_URL + "?state=WAITING&from=0&size=10")
                .header(USER_HEADER, booker.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookingResponse.class)
                .value(responses -> {
                    assertThat(responses).hasSize(2);
                    // sorted by start descending
                    assertThat(responses.get(0).getId()).isEqualTo(booking3.getId());
                    assertThat(responses.get(1).getId()).isEqualTo(booking.getId());
                });
    }

    @Test
    void getBookingsOfUser_statePAST_whenHasBooking_returnsCorrectList() {
        User owner = saveAndGetUser("owner@email.com");
        User booker = saveAndGetUser("booker@email.com");
        Item one = saveAndGetItemOfUser(owner);
        Item two = saveAndGetItemOfUser(owner);
        Item three = saveAndGetItemOfUser(owner);
        LocalDateTime startOne = LocalDateTime.now().minusDays(1);
        LocalDateTime endOne = LocalDateTime.now().plusDays(2);
        saveAndGetBooking(booker, one, WAITING, startOne, endOne); // CURRENT
        saveAndGetBooking(booker, two, APPROVED, startOne.plusDays(3), endOne.plusDays(5)); // FUTURE
        Booking booking = saveAndGetBooking(booker, three, REJECTED, startOne.minusDays(10), endOne.minusDays(10)); // PAST

        webTestClient.get().uri(BOOKINGS_BASE_URL + "?state=PAST&from=0&size=10")
                .header(USER_HEADER, booker.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookingResponse.class)
                .value(responses -> {
                    assertThat(responses).hasSize(1);
                    assertThat(responses.get(0).getId()).isEqualTo(booking.getId());
                });
    }

    @Test
    void getBookingsOfUser_stateFUTURE_whenHasBooking_returnsCorrectList() {
        User owner = saveAndGetUser("owner@email.com");
        User booker = saveAndGetUser("booker@email.com");
        Item one = saveAndGetItemOfUser(owner);
        Item two = saveAndGetItemOfUser(owner);
        Item three = saveAndGetItemOfUser(owner);
        LocalDateTime startOne = LocalDateTime.now().minusDays(1);
        LocalDateTime endOne = LocalDateTime.now().plusDays(2);
        saveAndGetBooking(booker, one, WAITING, startOne, endOne); // CURRENT
        Booking booking = saveAndGetBooking(booker, two, APPROVED, startOne.plusDays(3), endOne.plusDays(5)); // FUTURE
        saveAndGetBooking(booker, three, REJECTED, startOne.minusDays(10), endOne.minusDays(10)); // PAST

        webTestClient.get().uri(BOOKINGS_BASE_URL + "?state=FUTURE&from=0&size=10")
                .header(USER_HEADER, booker.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookingResponse.class)
                .value(responses -> {
                    assertThat(responses).hasSize(1);
                    assertThat(responses.get(0).getId()).isEqualTo(booking.getId());
                });
    }

    @Test
    void getBookingsOfUser_stateCURRENT_whenHasBooking_returnsCorrectList() {
        User owner = saveAndGetUser("owner@email.com");
        User booker = saveAndGetUser("booker@email.com");
        Item one = saveAndGetItemOfUser(owner);
        Item two = saveAndGetItemOfUser(owner);
        Item three = saveAndGetItemOfUser(owner);
        LocalDateTime startOne = LocalDateTime.now().minusDays(1);
        LocalDateTime endOne = LocalDateTime.now().plusDays(2);
        Booking booking = saveAndGetBooking(booker, one, WAITING, startOne, endOne); // CURRENT
        saveAndGetBooking(booker, two, APPROVED, startOne.plusDays(3), endOne.plusDays(5)); // FUTURE
        saveAndGetBooking(booker, three, REJECTED, startOne.minusDays(10), endOne.minusDays(10)); // PAST

        webTestClient.get().uri(BOOKINGS_BASE_URL + "?state=CURRENT&from=0&size=10")
                .header(USER_HEADER, booker.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookingResponse.class)
                .value(responses -> {
                    assertThat(responses).hasSize(1);
                    assertThat(responses.get(0).getId()).isEqualTo(booking.getId());
                });
    }

    @Test
    void getBookingsOfUser_stateALL_from1_size2_whenHasBookings_returnsCorrectList() {
        User owner = saveAndGetUser("owner@email.com");
        User booker = saveAndGetUser("booker@email.com");
        Item one = saveAndGetItemOfUser(owner);
        Item two = saveAndGetItemOfUser(owner);
        Item three = saveAndGetItemOfUser(owner);
        Booking booking = saveAndGetBooking(booker, one, WAITING, BOOKING_START, BOOKING_END);
        Booking booking2 = saveAndGetBooking(booker, two, APPROVED, BOOKING_START.plusDays(1), BOOKING_END.plusDays(1));
        saveAndGetBooking(booker, three, REJECTED, BOOKING_START.plusDays(2), BOOKING_END.plusDays(2));

        webTestClient.get().uri(BOOKINGS_BASE_URL + "?state=ALL&from=1&size=2")
                .header(USER_HEADER, booker.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookingResponse.class)
                .value(responses -> {
                    assertThat(responses).hasSize(2);
                    // sorted by start descending
                    assertThat(responses.get(0).getId()).isEqualTo(booking2.getId());
                    assertThat(responses.get(1).getId()).isEqualTo(booking.getId());
                });
    }

    @Test
    void getBookingsOfUser_stateALL_whenHasBookings_returnsCorrectList() {
        User owner = saveAndGetUser("owner@email.com");
        User booker = saveAndGetUser("booker@email.com");
        Item one = saveAndGetItemOfUser(owner);
        Item two = saveAndGetItemOfUser(owner);
        Item three = saveAndGetItemOfUser(owner);
        Booking booking = saveAndGetBooking(booker, one, WAITING, BOOKING_START, BOOKING_END);
        Booking booking2 = saveAndGetBooking(booker, two, APPROVED, BOOKING_START.plusDays(1), BOOKING_END.plusDays(1));
        Booking booking3 = saveAndGetBooking(booker, three, REJECTED, BOOKING_START.plusDays(2), BOOKING_END.plusDays(2));

        webTestClient.get().uri(BOOKINGS_BASE_URL + "?state=ALL&from=0&size=10")
                .header(USER_HEADER, booker.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookingResponse.class)
                .value(responses -> {
                    assertThat(responses).hasSize(3);
                    // sorted by start descending
                    assertThat(responses.get(0).getId()).isEqualTo(booking3.getId());
                    assertThat(responses.get(1).getId()).isEqualTo(booking2.getId());
                    assertThat(responses.get(2).getId()).isEqualTo(booking.getId());
                });
    }

    @Test
    void getBookingsOfUser_whenNoBookings_returnsEmptyList() {
        User booker = saveAndGetUser("owner@email.com");

        webTestClient.get().uri(BOOKINGS_BASE_URL + "?state=CURRENT&from=0&size=10")
                .header(USER_HEADER, booker.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookingResponse.class)
                .value(responses -> assertThat(responses).isEmpty());
    }

    @Test
    void updateStatus_updatesStatusOfBooking() {
        User booker = saveAndGetUser("booker@email.com");
        User owner = saveAndGetUser("owner@email.com");
        Item item = saveAndGetItemOfUser(owner);
        Booking booking = saveAndGetBooking(booker, item, WAITING, BOOKING_START, BOOKING_END);

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


    private Booking saveAndGetBooking(User booker, Item item, BookingStatus status,
                                      LocalDateTime start, LocalDateTime end) {
        Booking booking = getBookingBeforeSave(booker, item);
        booking.setStart(start);
        booking.setEnd(end);
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