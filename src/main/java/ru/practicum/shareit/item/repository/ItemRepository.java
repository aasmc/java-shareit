package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select i from Item i join fetch i.owner o where i.id = ?1")
    Optional<Item> findByIdOwnerFetched(Long itemId);

    @Query("select distinct i from Item i left join fetch i.bookings b where i.id = ?1")
    Optional<Item> findItemByIdWithBookingsFetched(Long itemId);

    @Query("select distinct i from Item i join fetch i.owner u left join fetch i.bookings b where u.id = :ownerId")
    List<Item> findAllByOwnerIdFetchBookings(@Param("ownerId") Long ownerId);

    @Query("select i from Item i join fetch i.owner o " +
            "where (upper(i.name) like upper(concat('%', ?1, '%')) " +
            "or upper(i.description) like upper(concat('%', ?1, '%'))) " +
            "and i.available = true")
    List<Item> searchAllItemFetchOwnerByQuery(String query);

    void deleteAllByOwner_Id(Long ownerId);

    void deleteItemByIdAndOwner_Id(Long itemId, Long ownerId);

}
