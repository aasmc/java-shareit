package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findAllByRequestor_Id(Long userId);

    @Query("select i from ItemRequest i where i.requestor.id != :userId")
    List<ItemRequest> findAllNotOfUser(Long userId, Pageable pageable);

}
