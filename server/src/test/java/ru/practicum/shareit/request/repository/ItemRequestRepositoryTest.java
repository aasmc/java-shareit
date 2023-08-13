package ru.practicum.shareit.request.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.BaseJpaTest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.OffsetBasedPageRequest;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static ru.practicum.shareit.testutil.TestDataProvider.getMockUser;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestRepositoryTest extends BaseJpaTest {

    private final ItemRequestRepository itemRequestRepository;
    private final TestEntityManager em;

    @Test
    void findAllNotOfUser_returnsCorrectList() {
        User requestor1 = saveAndReturnUser("requestor1@email.com");
        saveAndReturnItemRequest(requestor1);
        User requestor2 = saveAndReturnUser("requestor2@email.com");
        User requestor3 = saveAndReturnUser("requestor3@email.com");
        saveAndReturnItemRequest(requestor2);
        ItemRequest two = saveAndReturnItemRequest(requestor3);

        List<ItemRequest> result = itemRequestRepository.findAllNotOfUser(requestor1.getId(),
                new OffsetBasedPageRequest(1, 1));
        assertThat(result).hasSize(1);
        assertThat(result).contains(two);
    }

    private ItemRequest saveAndReturnItemRequest(User requestor) {
        ItemRequest req = new ItemRequest()
                .setRequestor(requestor)
                .setDescription("Item Description");
        return em.persistAndFlush(req);
    }

    private User saveAndReturnUser(String email) {
        User user = getMockUser(null);
        user.setEmail(email);
        return em.persistAndFlush(user);
    }

}