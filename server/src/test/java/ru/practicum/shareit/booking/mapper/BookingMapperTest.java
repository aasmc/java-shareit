package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.error.ServiceException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.testutil.TestDataProvider.*;

@ExtendWith(MockitoExtension.class)
class BookingMapperTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private BookingMapper mapper;

    @Test
    void mapToDomain_whenAllCorrect_returnsCorrectBooking() {
        User user = getMockUser(1L);
        Item item = getAvailableItemWithoutBookings();
        BookingRequest dto = getBookingRequest(item.getId(), user.getId());
        Mockito
                .when(userRepository.findById(dto.getBookerId())).thenReturn(Optional.of(user));
        Mockito
                .when(itemRepository.findByIdOwnerFetched(dto.getItemId())).thenReturn(Optional.of(item));
        Booking expected = Booking
                .builder()
                .id(dto.getId())
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(item)
                .booker(user)
                .status(dto.getStatus())
                .build();

        Booking actual = mapper.mapToDomain(dto);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToDomain_whenItemNotFound_throws() {
        BookingRequest dto = getBookingRequest(10L, 10L);
        Mockito
                .when(userRepository.findById(dto.getBookerId())).thenReturn(Optional.of(new User()));
        Mockito
                .when(itemRepository.findByIdOwnerFetched(dto.getItemId())).thenReturn(Optional.empty());
        assertThrows(ServiceException.class, () -> mapper.mapToDomain(dto));
    }

    @Test
    void mapToDomain_whenUserNotFound_throws() {
        BookingRequest dto = getBookingRequest(10L, 10L);
        Mockito
                .when(userRepository.findById(dto.getBookerId())).thenReturn(Optional.empty());

        assertThrows(ServiceException.class, () -> mapper.mapToDomain(dto));
    }
}