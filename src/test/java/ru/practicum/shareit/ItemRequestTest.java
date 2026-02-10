package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ItemRequestTest {
    private ItemRequestRepository requestRepository;
    private User requestor;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        requestRepository = new ItemRequestRepository();

        requestor = User.builder()
                .id(1L)
                .name("User 1")
                .email("user1@email.com")
                .build();

        now = LocalDateTime.now();
    }

    // сохранение нового запроса
    @Test
    void save_shouldSaveNewRequestAndAssignId() {
        ItemRequest request = ItemRequest.builder()
                .description("Чёта нада")
                .requestor(requestor)
                .created(now)
                .build();


        ItemRequest savedRequest = requestRepository.save(request);

        assertNotNull(savedRequest.getId(), "Сохраненному запросу должен быть присвоен ID");
        assertEquals("Чёта нада", savedRequest.getDescription(),
                "Описание должно сохраниться");
        assertEquals(requestor.getId(), savedRequest.getRequestor().getId(),
                "ID создателя запроса должно сохраниться");
        assertEquals(now, savedRequest.getCreated(), "Дата создания должна сохраниться");
    }

    // поиск запроса по id
    @Test
    void findById_shouldReturnRequestWhenExists() {
        ItemRequest request = ItemRequest.builder()
                .description("Чёта нада")
                .requestor(requestor)
                .created(now)
                .build();
        ItemRequest savedRequest = requestRepository.save(request);

        Optional<ItemRequest> foundRequest = requestRepository.findById(savedRequest.getId());

        assertTrue(foundRequest.isPresent(), "Запрос должен быть найден");
        assertEquals(savedRequest.getId(), foundRequest.get().getId(), "ID должно совпадать");
        assertEquals("Чёта нада", foundRequest.get().getDescription(), "Описание должно совпадать");
    }

    // получение всех запросов пользователя
    @Test
    void findAllByRequestorId_shouldReturnUserRequestsSortedByDate() {
        ItemRequest olderRequest = ItemRequest.builder()
                .description("Чёта нада 1")
                .requestor(requestor)
                .created(now.minusDays(2))
                .build();

        ItemRequest newerRequest = ItemRequest.builder()
                .description("Чёта нада 2")
                .requestor(requestor)
                .created(now.minusDays(1))
                .build();

        requestRepository.save(olderRequest);
        requestRepository.save(newerRequest);

        List<ItemRequest> requests = requestRepository.findAllByRequestorId(requestor.getId());

        assertEquals(2, requests.size(), "Должны вернуться 2 запроса пользователя");

        // Проверяем сортировку от новых к старым
        assertEquals("Чёта нада 2", requests.get(0).getDescription(),
                "Первым должен быть Чёта нада 2");
        assertEquals("Чёта нада 1", requests.get(1).getDescription(),
                "Вторым должен быть Чёта нада 1");
    }


    // получение всех запросов
    @Test
    void findAll_shouldReturnAllRequests() {
        ItemRequest request1 = ItemRequest.builder()
                .description("Чёта нада 1")
                .requestor(requestor)
                .created(now)
                .build();

        ItemRequest request2 = ItemRequest.builder()
                .description("Чёта нада 2")
                .requestor(requestor)
                .created(now.plusHours(1))
                .build();

        requestRepository.save(request1);
        requestRepository.save(request2);

        List<ItemRequest> allRequests = requestRepository.findAll();

        assertEquals(2, allRequests.size(), "Должны вернуться все запросы");
    }

    // удаления запроса
    @Test
    void deleteById_shouldRemoveRequestFromRepository() {

        ItemRequest request = ItemRequest.builder()
                .description("Чёта нада")
                .requestor(requestor)
                .created(now)
                .build();
        ItemRequest savedRequest = requestRepository.save(request);

        // запрос существует
        assertTrue(requestRepository.findById(savedRequest.getId()).isPresent(),
                "Запрос должен существовать до удаления");

        requestRepository.deleteById(savedRequest.getId());

        assertTrue(requestRepository.findById(savedRequest.getId()).isEmpty(),
                "После удаления запроса нет");
    }
}
