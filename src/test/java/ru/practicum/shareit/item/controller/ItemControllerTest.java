package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.practicum.shareit.BaseIntegTest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.testutil.TestConstants.ITEMS_BASE_URL;
import static ru.practicum.shareit.testutil.TestConstants.USER_HEADER;
import static ru.practicum.shareit.testutil.TestDataProvider.*;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemControllerTest extends BaseIntegTest {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @SneakyThrows
    @Test
    void postComment_whenUserBookedItem_returnsCommentResponse() {
        User owner = getMockUser(null);
        User savedOwner = userRepository.save(owner);

        Item item = getItemNoBookingsNoRequest(null, true, savedOwner);
        Item savedItem = itemRepository.save(item);

        User unsavedBooker = getMockUser(null);
        unsavedBooker.setEmail("unsaved@email.com");
        User booker = userRepository.save(unsavedBooker);
        Booking booking = getBookingBeforeSave(booker, savedItem);
        bookingRepository.save(booking);

        CommentRequest request = CommentRequest.builder()
                .userId(booker.getId())
                .itemId(savedItem.getId())
                .text("Comment text")
                .build();

        mockMvc.perform(post(ITEMS_BASE_URL + "/{itemId}/comment", savedItem.getId())
                        .header(USER_HEADER, booker.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.text", is(request.getText())))
                .andExpect(jsonPath("$.authorName", is(booker.getName())))
                .andExpect(jsonPath("$.id").value(IsNull.notNullValue()))
                .andExpect(jsonPath("$.created").value(IsNull.notNullValue()));
    }

    @Test
    void searchItems_whenHasAvailableItems_returnsList() {
        User owner = getMockUser(null);
        User savedOwner = userRepository.save(owner);
        List<Item> savedItems = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Item item = getItemNoBookingsNoRequest(null, true, savedOwner);
            Item savedItem = itemRepository.save(item);
            savedItems.add(savedItem);
        }

        webTestClient.get().uri(ITEMS_BASE_URL + "/search?text=item")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(ItemDto.class)
                .value(items -> {
                    assertThat(items.size()).isEqualTo(savedItems.size());
                    items.sort(Comparator.comparingLong(ItemDto::getId));
                    for (int i = 0; i < items.size(); i++) {
                        ItemDto dto = items.get(i);
                        Item toCompare = savedItems.get(i);
                        assertThat(dto.getId()).isEqualTo(toCompare.getId());
                        assertThat(dto.getName()).isEqualTo(toCompare.getName());
                        assertThat(dto.getDescription()).isEqualTo(toCompare.getDescription());
                        assertThat(dto.getAvailable()).isEqualTo(toCompare.getAvailable());
                        assertThat(dto.getOwnerId()).isEqualTo(savedOwner.getId());
                    }
                });
    }

    @Test
    void searchItems_whenNoAvailableItems_returnsEmptyList() {
        User owner = getMockUser(null);
        User savedOwner = userRepository.save(owner);
        for (int i = 0; i < 5; i++) {
            Item item = getItemNoBookingsNoRequest(null, false, savedOwner);
            itemRepository.save(item);
        }

        webTestClient.get().uri(ITEMS_BASE_URL + "/search?text=item")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(ItemDto.class)
                .value(items -> {
                    assertThat(items).isEmpty();
                });
    }

    @Test
    void getItemsForUser_whenHasItems_returnsList() {
        User owner = getMockUser(null);
        User savedOwner = userRepository.save(owner);
        List<Item> savedItems = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Item item = getItemNoBookingsNoRequest(null, true, savedOwner);
            Item savedItem = itemRepository.save(item);
            savedItems.add(savedItem);
        }

        webTestClient.get().uri(ITEMS_BASE_URL)
                .header(USER_HEADER, savedOwner.getId().toString())
                .exchange()
                .expectBodyList(ItemDto.class)
                .value(items -> {
                    assertThat(items.size()).isEqualTo(savedItems.size());
                    items.sort(Comparator.comparingLong(ItemDto::getId));
                    for (int i = 0; i < items.size(); i++) {
                        ItemDto dto = items.get(i);
                        Item toCompare = savedItems.get(i);
                        assertThat(dto.getId()).isEqualTo(toCompare.getId());
                        assertThat(dto.getName()).isEqualTo(toCompare.getName());
                        assertThat(dto.getDescription()).isEqualTo(toCompare.getDescription());
                        assertThat(dto.getAvailable()).isEqualTo(toCompare.getAvailable());
                        assertThat(dto.getOwnerId()).isEqualTo(savedOwner.getId());
                    }
                });
    }

    @Test
    void getItemsForUser_whenNoItems_returnsEmptyList() {
        User owner = getMockUser(null);
        User savedOwner = userRepository.save(owner);

        webTestClient.get().uri(ITEMS_BASE_URL)
                .header(USER_HEADER, savedOwner.getId().toString())
                .exchange()
                .expectBodyList(ItemDto.class)
                .value(items -> {
                    assertThat(items).isEmpty();
                });
    }


    @Test
    void getItemById_returnsCorrectItem() {
        User owner = getMockUser(null);
        User savedOwner = userRepository.save(owner);
        Item item = getItemNoBookingsNoRequest(null, true, savedOwner);
        Item savedItem = itemRepository.save(item);

        webTestClient.get().uri(ITEMS_BASE_URL + "/" + savedItem.getId())
                .header(USER_HEADER, "10")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(ItemDto.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getName()).isEqualTo(savedItem.getName());
                    assertThat(response.getDescription()).isEqualTo(savedItem.getDescription());
                    assertThat(response.getAvailable()).isEqualTo(savedItem.getAvailable());
                    assertThat(response.getOwnerId()).isEqualTo(owner.getId());
                });
    }

    @Test
    void getItemById_whenItemNotFound_returns404() {
        webTestClient.get().uri(ITEMS_BASE_URL + "/1000")
                .header(USER_HEADER, "1")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateItem_whenItemNotBelongsToUser_returns403() {
        User owner = getMockUser(null);
        User savedOwner = userRepository.save(owner);
        Item item = getItemNoBookingsNoRequest(null, true, savedOwner);
        Item savedItem = itemRepository.save(item);
        ItemDto toUpdate = getItemDtoForUpdate(!savedItem.getAvailable());

        User user = getMockUser(null);
        user.setEmail("newemail@email.com");
        User savedUser = userRepository.save(user);

        webTestClient.patch().uri(ITEMS_BASE_URL + "/" + savedItem.getId())
                .header(USER_HEADER, savedUser.getId().toString())
                .bodyValue(toUpdate)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void updateItem_whenAllCorrect_returnsUpdated() {
        User owner = getMockUser(null);
        User savedOwner = userRepository.save(owner);
        Item item = getItemNoBookingsNoRequest(null, true, savedOwner);
        Item savedItem = itemRepository.save(item);
        ItemDto toUpdate = getItemDtoForUpdate(!savedItem.getAvailable());

        webTestClient.patch().uri(ITEMS_BASE_URL + "/" + savedItem.getId())
                .header(USER_HEADER, owner.getId().toString())
                .bodyValue(toUpdate)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(ItemDto.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getName()).isEqualTo(toUpdate.getName());
                    assertThat(response.getDescription()).isEqualTo(toUpdate.getDescription());
                    assertThat(response.getAvailable()).isEqualTo(toUpdate.getAvailable());
                    assertThat(response.getOwnerId()).isEqualTo(owner.getId());
                });
    }

    @Test
    void createItem_whenAllGood_returnsCreatedItem() {
        ItemDto dto = getItemDtoForCreate(null);
        User user = getMockUser(null);
        User saved = userRepository.save(user);

        webTestClient.post().uri(ITEMS_BASE_URL)
                .header(USER_HEADER, saved.getId().toString())
                .bodyValue(dto)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(ItemDto.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getName()).isEqualTo(dto.getName());
                    assertThat(response.getDescription()).isEqualTo(dto.getDescription());
                    assertThat(response.getOwnerId()).isEqualTo(saved.getId());
                });
    }

    @Test
    void createItem_whenAvailableIsNull_returns400() {
        ItemDto dto = getItemDtoForCreate(null);
        dto.setAvailable(null);
        User user = getMockUser(null);
        user = userRepository.save(user);

        webTestClient.post().uri(ITEMS_BASE_URL)
                .header(USER_HEADER, user.getId().toString())
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createItem_whenEmptyDescription_returns400() {
        ItemDto dto = getItemDtoForCreate(null);
        dto.setDescription("");
        User user = getMockUser(null);
        user = userRepository.save(user);

        webTestClient.post().uri(ITEMS_BASE_URL)
                .header(USER_HEADER, user.getId().toString())
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createItem_whenEmptyName_returns400() {
        ItemDto dto = getItemDtoForCreate(null);
        dto.setName("");
        User user = getMockUser(null);
        user = userRepository.save(user);

        webTestClient.post().uri(ITEMS_BASE_URL)
                .header(USER_HEADER, user.getId().toString())
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createItem_whenUserNotFound_returns404() {
        ItemDto dto = getItemDtoForCreate(null);
        webTestClient.post().uri(ITEMS_BASE_URL)
                .header(USER_HEADER, "999")
                .bodyValue(dto)
                .exchange()
                .expectStatus().isNotFound();
    }

}