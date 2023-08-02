package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.practicum.shareit.BaseIntegTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static ru.practicum.shareit.testutil.TestConstants.REQUESTS_BASE_URL;
import static ru.practicum.shareit.testutil.TestConstants.USER_HEADER;
import static ru.practicum.shareit.testutil.TestDataProvider.*;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestControllerTest extends BaseIntegTest {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Test
    void getAllRequests_whenRequestsExist_returnsListOfRequestsOfOtherUsers() {
        User one = saveAndGetUser("one@mail.com");
        User two = saveAndGetUser("two@mail.com");
        User three = saveAndGetUser("three@mail.com");
        User four = saveAndGetUser("four@mail.com");
        User five = saveAndGetUser("five@mail.com");

        saveAndGetItemRequest(one);
        saveAndGetItemRequest(two);
        ItemRequest reqThree = saveAndGetItemRequest(three);
        ItemRequest reqFour = saveAndGetItemRequest(four);
        saveAndGetItemRequest(five);

        webTestClient
                // get two requests of user with id != one.getId() starting from second request
                .get().uri(REQUESTS_BASE_URL + "/all?from=1&size=2")
                .header(USER_HEADER, one.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ItemRequestResponse.class)
                .value(responses -> {
                    assertThat(responses).hasSize(2);
                    ItemRequestResponse response = responses.get(0);
                    assertThat(response.getId()).isEqualTo(reqFour.getId());
                    assertThat(response.getDescription()).isEqualTo(reqFour.getDescription());

                    ItemRequestResponse response2 = responses.get(1);
                    assertThat(response2.getId()).isEqualTo(reqThree.getId());
                    assertThat(response2.getDescription()).isEqualTo(reqThree.getDescription());
                });
    }

    @Test
    void getRequest_whenExists_returnsResponse() {
        User user = saveAndGetDefaultRequestor();
        ItemRequest request = saveAndGetItemRequest(user);

        webTestClient
                .get().uri(REQUESTS_BASE_URL + "/" + request.getId())
                .header(USER_HEADER, user.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(ItemRequestResponse.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getDescription()).isEqualTo(request.getDescription());
                    assertThat(response.getCreated()).isBefore(LocalDateTime.now());
                });
    }

    @Test
    void getRequestsOfUser_whenUserExists_hasRequestsWithMatchedItems_returnsCorrectList() {
        User user = saveAndGetDefaultRequestor();
        User owner = saveAndGetUser("owner1@mail.com");
        User owner2 = saveAndGetUser("owner2@mail.com");
        ItemRequest request1 = saveAndGetItemRequest(user);
        Item itemOne = saveAndGetItem(owner, request1);
        ItemRequest request2 = saveAndGetItemRequest(user);
        Item itemTwo = saveAndGetItem(owner2, request2);

        webTestClient
                .get().uri(REQUESTS_BASE_URL)
                .header(USER_HEADER, user.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ItemRequestResponse.class)
                .value(responses -> {
                    assertThat(responses).hasSize(2);
                    ItemRequestResponse response1 = responses.get(0);
                    assertThat(response1.getId()).isEqualTo(request1.getId());
                    assertThat(response1.getDescription()).isEqualTo(request1.getDescription());
                    assertThat(response1.getItems()).hasSize(1);
                    assertThat(response1.getItems().get(0).getId()).isEqualTo(itemOne.getId());

                    ItemRequestResponse response2 = responses.get(1);
                    assertThat(response2.getId()).isEqualTo(request2.getId());
                    assertThat(response2.getDescription()).isEqualTo(request2.getDescription());
                    assertThat(response2.getItems()).hasSize(1);
                    assertThat(response2.getItems().get(0).getId()).isEqualTo(itemTwo.getId());
                });
    }

    @Test
    void getRequestsOfUser_whenUserExists_hasRequestsAndNoItems_returnsCorrectList() {
        User user = saveAndGetDefaultRequestor();
        ItemRequest request1 = saveAndGetItemRequest(user);
        ItemRequest request2 = saveAndGetItemRequest(user);

        webTestClient
                .get().uri(REQUESTS_BASE_URL)
                .header(USER_HEADER, user.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ItemRequestResponse.class)
                .value(responses -> {
                    assertThat(responses).hasSize(2);
                    assertThat(responses.get(0).getId()).isEqualTo(request1.getId());
                    assertThat(responses.get(1).getId()).isEqualTo(request2.getId());
                });
    }

    @Test
    void getRequestsOfUser_whenUserExists_noRequests_returnsEmptyList() {
        User user = saveAndGetDefaultRequestor();
        webTestClient
                .get().uri(REQUESTS_BASE_URL)
                .header(USER_HEADER, user.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ItemRequestResponse.class)
                .value(responses -> assertThat(responses).isEmpty());
    }

    @Test
    void getRequestsOfUser_whenUserNotExists_returns404() {
        webTestClient
                .get().uri(REQUESTS_BASE_URL)
                .header(USER_HEADER, "1000")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void addRequest_whenUserExists_createsRequest() {
        User user = saveAndGetDefaultRequestor();
        ItemRequestDto dto = getitemRequestDto(null);

        webTestClient.post().uri(REQUESTS_BASE_URL)
                .header(USER_HEADER, user.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ItemRequestResponse.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getCreated()).isBefore(LocalDateTime.now());
                    assertThat(response.getDescription()).isEqualTo(dto.getDescription());
                    assertThat(response.getItems()).isEmpty();
                });
    }

    @Test
    void addRequest_whenUserNotExists_returns404() {
        ItemRequestDto dto = getitemRequestDto(null);
        webTestClient.post().uri(REQUESTS_BASE_URL)
                .header(USER_HEADER, "1000")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isNotFound();
    }

    private ItemRequest saveAndGetItemRequest(User requestor) {
        ItemRequest request = ItemRequest.builder()
                .description("Item Description")
                .requestor(requestor)
                .build();
        return itemRequestRepository.save(request);
    }

    private Item saveAndGetItem(User owner, ItemRequest request) {
        Item item = getTransientAvailableItemNoBookings(owner, request);
        return itemRepository.save(item);
    }

    private User saveAndGetDefaultRequestor() {
        return saveAndGetUser("requestor@email.com");
    }

    private User saveAndGetUser(String email) {
        User user = getMockUser(null);
        user.setEmail(email);
        return userRepository.save(user);
    }

}