package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
    }

    // сохранение нового пользователя
    @Test
    void save_shouldSaveNewUser() {
        User user = User.builder()
                .name("User 1")
                .email("user1@email.com")
                .build();

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId(), "Сохраненному пользователю должен быть присвоен ID");

        assertEquals("User 1", savedUser.getName(), "Имя должно сохраниться");
        assertEquals("user1@email.com", savedUser.getEmail(), "Email должен сохраниться");
    }

    // проверка уникальности email при сохранении
    @Test
    void save_shouldThrowExceptionWhenEmailExists() {
        User user1 = User.builder()
                .name("User 1")
                .email("user1@email.com")
                .build();
        userRepository.save(user1);

        // Создаем второго пользователя с таким же email
        User user2 = User.builder()
                .name("User 2")
                .email("user1@email.com")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> userRepository.save(user2),
                "Должно быть выброшено исключение при попытке сохранить пользователя с существующим email");
    }

    // поиска пользователя по ID
    @Test
    void findById_shouldReturnUserWhenExists() {
        User user = User.builder()
                .name("User 1")
                .email("user1@email.com")
                .build();
        User savedUser = userRepository.save(user);

        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertTrue(foundUser.isPresent(), "Пользователь должен быть найден");
        assertEquals(savedUser.getId(), foundUser.get().getId(), "ID найденного пользователя должен совпадать");
        assertEquals("User 1", foundUser.get().getName(), "Имя должно совпадать");
    }

    // поиска несуществующего пользователя
    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        Optional<User> foundUser = userRepository.findById(999L);

        assertTrue(foundUser.isEmpty(), "Для несуществующего ID должен вернуться пустой Optional");
    }

    // обновление пользователя
    @Test
    void update_shouldUpdateUser() {
        User user = User.builder()
                .name("User 1")
                .email("user1@email.com")
                .build();
        User savedUser = userRepository.save(user);

        // Создаем объект с обновленными данными
        User updatedUser = User.builder()
                .id(savedUser.getId())
                .name("User Update")
                .email("userUpdate@email.com")
                .build();

        User result = userRepository.update(updatedUser);

        assertEquals("User Update", result.getName(), "Имя должно обновиться");
        assertEquals("userUpdate@email.com", result.getEmail(), "Email должен обновиться");
    }

    // удаление пользователя
    @Test
    void deleteById_shouldDeleteUser() {
        User user = User.builder()
                .name("User 1")
                .email("user1@email.com")
                .build();
        User savedUser = userRepository.save(user);

        userRepository.deleteById(savedUser.getId());

        assertTrue(userRepository.findById(savedUser.getId()).isEmpty(),
                "После удаления пользователя нет");
    }

    // получение всех пользователей
    @Test
    void findAll_shouldReturnAllUsers() {

        User user1 = User.builder()
                .name("User 1")
                .email("user1@email.com")
                .build();

        User user2 = User.builder()
                .name("User 2")
                .email("user2@email.com")
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        List<User> users = userRepository.findAll();

        assertEquals(2, users.size(), "Должно вернуться 2 пользователя");

        boolean hasJohn = users.stream().anyMatch(u -> u.getName().equals("User 1"));
        boolean hasJane = users.stream().anyMatch(u -> u.getName().equals("User 2"));

        assertTrue(hasJohn, "В списке должен быть пользователь User 1");
        assertTrue(hasJane, "В списке должен быть пользователь User 2");
    }
}
