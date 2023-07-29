package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.BaseJpaTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.OffsetBasedPageRequest;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static ru.practicum.shareit.testutil.TestDataProvider.*;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRepositoryTest extends BaseJpaTest {


    private final ItemRepository itemRepository;
    private final TestEntityManager em;

    @Test
    void searchAllAvailableItemsFetchOwnerByQuery_returnsOnlyItemsFittingInPage() {
        User owner = getAndSaveUser("owner@email.com");
        User requestor = getAndSaveUser("requestor@email.com");
        ItemRequest itemRequest = getAndSaveItemRequest(requestor);
        getAndSaveItem(owner, itemRequest);
        Item item2 = getAndSaveItem(owner, itemRequest);
        Item item3 = getAndSaveItem(owner, itemRequest);
        getAndSaveItem(owner, itemRequest);

        List<Item> items = itemRepository.searchAllAvailableItemsFetchOwnerByQuery("item",
               new OffsetBasedPageRequest(1, 2, Sort.by(Sort.Direction.ASC, "id")));
        assertThat(items.size()).isEqualTo(2);
        assertThat(items.get(0).getId()).isEqualTo(item2.getId());
        assertThat(items.get(0).getName()).isEqualTo(item2.getName());
        assertThat(items.get(0).getDescription()).isEqualTo(item2.getDescription());

        assertThat(items.get(1).getId()).isEqualTo(item3.getId());
        assertThat(items.get(1).getName()).isEqualTo(item3.getName());
        assertThat(items.get(1).getDescription()).isEqualTo(item3.getDescription());
    }

    @Test
    void searchAllAvailableItemsFetchOwnerByQuery_returnsCorrectList() {
        User owner = getAndSaveUser("owner@email.com");
        User requestor = getAndSaveUser("requestor@email.com");
        ItemRequest itemRequest = getAndSaveItemRequest(requestor);
        Item item = getAndSaveItem(owner, itemRequest);

        List<Item> items = itemRepository.searchAllAvailableItemsFetchOwnerByQuery("item",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")));
        assertThat(items.size()).isEqualTo(1);
        assertThat(items.get(0).getId()).isEqualTo(item.getId());
        assertThat(items.get(0).getName()).isEqualTo(item.getName());
        assertThat(items.get(0).getDescription()).isEqualTo(item.getDescription());
    }

    @Test
    void searchAllAvailableItemsFetchOwnerByQuery_whenNoAvailableItems_returnsEmptyList() {
        User owner = getAndSaveUser("owner@email.com");
        User requestor = getAndSaveUser("requestor@email.com");
        ItemRequest itemRequest = getAndSaveItemRequest(requestor);
        Item item = getAndSaveItem(owner, itemRequest);
        item.setAvailable(false);
        em.persistAndFlush(item);
        List<Item> items = itemRepository.searchAllAvailableItemsFetchOwnerByQuery("item",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")));
        assertThat(items).isEmpty();
    }

    private Item getAndSaveItem(User owner, ItemRequest itemRequest) {
        Item item = getAvailableItemWithoutBookings();
        item.setId(null);
        item.setOwner(owner);
        item.setRequest(itemRequest);
        return em.persist(item);
    }

    private ItemRequest getAndSaveItemRequest(User requestor) {
        ItemRequest itemRequest = getItemRequest();
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