package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class IntegrationTest {
    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ItemRepository itemRepository;

    @Autowired
    protected BookingRepository bookingRepository;

    @Autowired
    protected CommentRepository commentRepository;

    @BeforeEach
    void cleanDatabase() {
        // Очищаем все таблицы перед каждым тестом
        commentRepository.deleteAll();
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }
}
