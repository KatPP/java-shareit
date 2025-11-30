import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ShareItServer.class)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private UserDto user1;
    private UserDto user2;

    @BeforeEach
    void setUp() {
        user1 = new UserDto(null, "User 1", "user1@example.com");
        user1 = userService.create(user1);

        user2 = new UserDto(null, "User 2", "user2@example.com");
        user2 = userService.create(user2);
    }

    @Test
    void createRequest_shouldSaveAndReturnWithEmptyItems() {
        String description = "Нужен шуруповёрт";
        ItemRequestResponseDto response = itemRequestService.create(user1.getId(), description);
        assertNotNull(response.getId());
        assertEquals(description, response.getDescription());
        assertNotNull(response.getCreated());
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    void getOwnRequests_shouldReturnRequestsCreatedThisUser() {
        itemRequestService.create(user1.getId(), "Запрос 1");
        itemRequestService.create(user1.getId(), "Запрос 2");
        itemRequestService.create(user2.getId(), "Запрос 3");

        List<ItemRequestResponseDto> requests = itemRequestService.getOwnRequests(user1.getId());
        assertEquals(2, requests.size());
        assertEquals("Запрос 2", requests.get(0).getDescription());
        assertEquals("Запрос 1", requests.get(1).getDescription());
    }

    @Test
    void getAllOtherRequests_shouldExcludeOwnRequestsAndPaginate() {
        itemRequestService.create(user1.getId(), "Мой запрос");
        itemRequestService.create(user2.getId(), "Чужой запрос 1");
        itemRequestService.create(user2.getId(), "Чужой запрос 2");

        List<ItemRequestResponseDto> others = itemRequestService.getAllOtherRequests(user1.getId(), 0, 10);
        assertEquals(2, others.size());
        assertEquals("Чужой запрос 2", others.get(0).getDescription());
    }
}