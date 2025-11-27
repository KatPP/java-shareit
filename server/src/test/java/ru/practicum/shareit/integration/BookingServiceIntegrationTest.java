package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;

    @Test
    void createBooking_shouldCreateAndReturnBooking() {
        // Given
        UserDto user1 = new UserDto(null, "Booker", "booker@test.com");
        UserDto user2 = new UserDto(null, "Owner", "owner@test.com");
        UserDto savedUser1 = userService.create(user1);
        UserDto savedUser2 = userService.create(user2);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Профессиональная");
        itemDto.setAvailable(true);
        ItemDto savedItem = itemService.create(savedUser2.getId(), itemDto);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(savedItem.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        // When
        BookingResponseDto result = bookingService.create(savedUser1.getId(), bookingDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getItem().getId()).isEqualTo(savedItem.getId());
        assertThat(result.getBooker().getId()).isEqualTo(savedUser1.getId());
        assertThat(result.getStatus()).isEqualTo(ru.practicum.shareit.booking.model.BookingStatus.WAITING);
    }
}