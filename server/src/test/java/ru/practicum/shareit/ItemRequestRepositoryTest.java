package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestRepositoryTest extends RepositoryTest {

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private TestEntityManager em;

    private User requestor1;
    private User requestor2;
    private ItemRequest request1;
    private ItemRequest request2;

    @BeforeEach
    void setUp() {
        requestor1 = User.builder()
                .name("User1")
                .email("user1@email.com")
                .build();

        requestor2 = User.builder()
                .name("User2")
                .email("user2@email.com")
                .build();

        em.persist(requestor1);
        em.persist(requestor2);

        request1 = ItemRequest.builder()
                .description("Нужна дрель")
                .requestor(requestor1)
                .created(LocalDateTime.now().minusDays(1))
                .build();

        request2 = ItemRequest.builder()
                .description("Нужна лестница")
                .requestor(requestor2)
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void findAllByRequestorId_shouldReturnRequests() {
        em.persist(request1);
        em.persist(request2);

        List<ItemRequest> requests = requestRepository.findAllByRequestorId(
                requestor1.getId(), Sort.by(Sort.Direction.DESC, "created"));

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getDescription()).isEqualTo("Нужна дрель");
        assertThat(requests.get(0).getRequestor().getId()).isEqualTo(requestor1.getId());
    }

    @Test
    void findAllExceptRequestor_shouldReturnOtherUsersRequests() {
        em.persist(request1);
        em.persist(request2);

        List<ItemRequest> requests = requestRepository.findAllExceptRequestor(
                requestor1.getId(), Sort.by(Sort.Direction.DESC, "created"));

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getDescription()).isEqualTo("Нужна лестница");
        assertThat(requests.get(0).getRequestor().getId()).isEqualTo(requestor2.getId());
    }

    @Test
    void findAllExceptRequestor_whenNoOtherRequests_shouldReturnEmptyList() {
        em.persist(request1);

        List<ItemRequest> requests = requestRepository.findAllExceptRequestor(
                requestor1.getId(), Sort.by(Sort.Direction.DESC, "created"));

        assertThat(requests).isEmpty();
    }

    @Test
    void saveItemRequest_shouldSetId() {
        ItemRequest saved = requestRepository.save(request1);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDescription()).isEqualTo("Нужна дрель");
        assertThat(saved.getRequestor().getId()).isEqualTo(requestor1.getId());
        assertThat(saved.getCreated()).isNotNull();
    }

    @Test
    void findById_shouldReturnRequest() {
        em.persist(request1);

        ItemRequest found = requestRepository.findById(request1.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getDescription()).isEqualTo("Нужна дрель");
    }
}