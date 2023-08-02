package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.BaseJpaTest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static ru.practicum.shareit.testutil.TestDataProvider.*;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CommentRepositoryTest extends BaseJpaTest {


    private final CommentRepository commentRepository;
    private final TestEntityManager em;


    @Test
    void test_findAllByItem_IdIn() {
        User author = getAndSaveUser("author@email.com");
        User owner = getAndSaveUser("owner@email.com");
        User requestor = getAndSaveUser("requestor@email.con");
        ItemRequest itemRequest = getAndSaveItemRequest(requestor);
        Item item = getAndSaveItem(owner, itemRequest);
        Comment comment = getAndSaveComment(item, author);

        List<Comment> comments = commentRepository.findAllByItem_IdIn(Set.of(item.getId()));
        assertThat(comments.size()).isEqualTo(1);
        assertThat(comments.get(0).getId()).isEqualTo(comment.getId());
    }

    private Comment getAndSaveComment(Item item, User author) {
        Comment comment = new Comment();
        comment.setText("text");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return em.persist(comment);
    }

    private Item getAndSaveItem(User owner, ItemRequest itemRequest) {
        Item item = getAvailableItemWithoutBookings();
        item.setId(null);
        item.setOwner(owner);
        item.setRequest(itemRequest);
        return em.persist(item);
    }

    private ItemRequest getAndSaveItemRequest(User requestor) {
        ItemRequest itemRequest = getDefaultItemRequest();
        itemRequest.setId(null);
        itemRequest.setRequestor(requestor);
        return em.persist(itemRequest);
    }

    private User getAndSaveUser(String email) {
        User user = getMockUser(null);
        user.setEmail(email);
        return em.persist(user);
    }


}