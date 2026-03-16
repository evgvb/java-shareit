package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;  // Добавляем мок для itemRepository

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void getAllItemRequests_ShouldApplyPaginationAndIncludeItems() {
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Создаем запросы с разными датами для проверки сортировки
        LocalDateTime now = LocalDateTime.now();
        List<ItemRequest> mockRequests = List.of(
                createRequestWithDate(1L, now.minusDays(5)),
                createRequestWithDate(2L, now.minusDays(4)),
                createRequestWithDate(3L, now.minusDays(3)),
                createRequestWithDate(4L, now.minusDays(2)),
                createRequestWithDate(5L, now.minusDays(1))
        );

        when(itemRequestRepository.findAllExceptRequestor(eq(userId), any(Sort.class)))
                .thenReturn(mockRequests);

        // Тест 1: Проверка пагинации с from=1, size=2
        List<ItemRequestDto> result1 = itemRequestService.getAllItemRequests(userId, 1, 2);

        assertThat(result1).hasSize(2);
        assertThat(result1.get(0).getId()).isEqualTo(2L);
        assertThat(result1.get(1).getId()).isEqualTo(3L);

        // Тест 2: Проверка пагинации с from=3, size=2
        List<ItemRequestDto> result2 = itemRequestService.getAllItemRequests(userId, 3, 2);

        assertThat(result2).hasSize(2);
        assertThat(result2.get(0).getId()).isEqualTo(4L);
        assertThat(result2.get(1).getId()).isEqualTo(5L);

        // Тест 3: Проверка пагинации с from=0, size=3
        List<ItemRequestDto> result3 = itemRequestService.getAllItemRequests(userId, 0, 3);

        assertThat(result3).hasSize(3);
        assertThat(result3.get(0).getId()).isEqualTo(1L);
        assertThat(result3.get(1).getId()).isEqualTo(2L);
        assertThat(result3.get(2).getId()).isEqualTo(3L);
    }

    private ItemRequest createRequestWithDate(Long id, LocalDateTime created) {
        User requestor = User.builder()
                .id(id + 100)
                .name("User " + (id + 100))
                .email("user" + (id + 100) + "@email.com")
                .build();

        return ItemRequest.builder()
                .id(id)
                .description("описание " + id)
                .created(created)
                .requestor(requestor)
                .build();
    }
}