package ru.practicum.shareit.user.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.BaseIntegTest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.shareit.testutil.TestConstants.USERS_BASE_URL;
import static ru.practicum.shareit.testutil.TestDataProvider.getUserDto;

class UserControllerTest extends BaseIntegTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void whenDeleteUser_userNoLongerFound() {
        User user = getUserFromDb("mail@mail.com");

        webTestClient.delete().uri(USERS_BASE_URL + "/" + user.getId())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get().uri(USERS_BASE_URL + "/" + user.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenUpdateUser_ifOtherUserWithSameEmailExists_fails() {
        User user = getUserFromDb("mail@mail.com");
        User userTwo = getUserFromDb("othermail@mail.com");
        UserDto toUpdate = getUserDto();
        toUpdate.setEmail(user.getEmail());

        webTestClient.patch().uri(USERS_BASE_URL + "/" + userTwo.getId())
                .bodyValue(toUpdate)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void whenUpdateUserWithSameEmail_success() {
        User user = getUserFromDb("someuser@email.com");
        UserDto dto = getUserDto();
        dto.setEmail(user.getEmail());
        webTestClient.patch().uri(USERS_BASE_URL + "/" + user.getId())
                .bodyValue(dto)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(UserDto.class).value(response -> {
                    assertThat(response.getId()).isEqualTo(user.getId());
                    assertThat(response.getName()).isEqualTo(dto.getName());
                    assertThat(response.getEmail()).isEqualTo(dto.getEmail());
                });
    }

    @Test
    void whenUpdateGood_userUpdated() {
        User user = getUserFromDb("someuser@email.com");
        UserDto dto = getUserDto();
        webTestClient.patch().uri(USERS_BASE_URL + "/" + user.getId())
                .bodyValue(dto)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(UserDto.class).value(response -> {
                    assertThat(response.getId()).isEqualTo(user.getId());
                    assertThat(response.getName()).isEqualTo(dto.getName());
                    assertThat(response.getEmail()).isEqualTo(dto.getEmail());
                });
    }

    @Test
    void whenUpdateUser_userNotExists_fails() {
        UserDto dto = getUserDto();
        webTestClient.patch().uri(USERS_BASE_URL + "/999")
                .bodyValue(dto)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenGetAllUsers_usersInDb_returnsCorrectList() {
        User userOne = getUserFromDb("username@email.com");
        User userTwo = getUserFromDb("anothermail@email.com");
        webTestClient.get().uri(USERS_BASE_URL)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(UserDto.class).value(users -> {
                    assertThat(users.size()).isEqualTo(2);
                    Map<Long, UserDto> userMap = users.stream()
                            .collect(Collectors.toMap(UserDto::getId, Function.identity()));
                    UserDto dtoOne = userMap.get(userOne.getId());
                    UserDto dtoTwo = userMap.get(userTwo.getId());
                    assertThat(dtoOne).isNotNull();
                    assertThat(dtoOne.getId()).isEqualTo(userOne.getId());
                    assertThat(dtoOne.getEmail()).isEqualTo(userOne.getEmail());
                    assertThat(dtoOne.getName()).isEqualTo(userOne.getName());

                    assertThat(dtoTwo).isNotNull();
                    assertThat(dtoTwo.getId()).isEqualTo(userTwo.getId());
                    assertThat(dtoTwo.getEmail()).isEqualTo(userTwo.getEmail());
                    assertThat(dtoTwo.getName()).isEqualTo(userTwo.getName());
                });
    }

    @Test
    void whenGetAllUsers_noUsersInDb_returnsEmptyList() {
        webTestClient.get().uri(USERS_BASE_URL)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(UserDto.class).value(users -> {
                    assertThat(users).isEmpty();
                });
    }

    @Test
    void whenGetById_userNotExists_returnsNotFound() {
        webTestClient.get().uri(USERS_BASE_URL + "/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenGetById_userExists_returnsUser() {
        User user = getUserFromDb("username@email.com");
        webTestClient.get().uri(USERS_BASE_URL + "/" + user.getId())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(UserDto.class).value(dto -> {
                    assertThat(dto.getId()).isEqualTo(user.getId());
                    assertThat(dto.getName()).isEqualTo(user.getName());
                    assertThat(dto.getEmail()).isEqualTo(user.getEmail());
                });
    }

    @Test
    void whenCreateUserNoName_fails() {
        UserDto user = getUserDto();
        user.setName(null);
        webTestClient.post().uri(USERS_BASE_URL)
                .bodyValue(user)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void whenCreateUserNoEmail_fails() {
        UserDto user = getUserDto();
        user.setName(null);
        webTestClient.post().uri(USERS_BASE_URL)
                .bodyValue(user)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void whenCreateUserInvalidEmail_fails() {
        UserDto user = getUserDto();
        user.setEmail("invalidmail");

        webTestClient.post().uri(USERS_BASE_URL)
                .bodyValue(user)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void whenCreateUserWithDuplicateEmail_fails() {
        UserDto duplicateEmail = getUserDto();
        User user = User.builder()
                .email(duplicateEmail.getEmail())
                .name("name")
                .build();
        userRepository.save(user);

        webTestClient.post().uri(USERS_BASE_URL)
                .bodyValue(duplicateEmail)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void whenCreateValidUser_responseCreated() {
        UserDto expected = getUserDto();

        UserDto response = webTestClient.post().uri(USERS_BASE_URL)
                .bodyValue(expected)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(UserDto.class)
                .returnResult().getResponseBody();
        assertThat(response).isNotNull();
        assertThat(expected.getEmail()).isEqualTo(response.getEmail());
        assertThat(expected.getName()).isEqualTo(response.getName());
        assertThat(response.getId()).isNotNull();
    }

    private User getUserFromDb(String email) {
        User user = User.builder()
                .name("username")
                .email(email)
                .build();
        return userRepository.save(user);
    }

}