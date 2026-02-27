package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserTest extends IntegrationTest {
    @Autowired
    private UserService userService;

    @Test
    void createUser_ShouldSaveUser_WhenDataIsValid() {
        UserDto userDto = UserDto.builder()
                .name("Иван Петров")
                .email("ivan@example.com")
                .build();

        UserDto createdUser = userService.createUser(userDto);

        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getName()).isEqualTo("Иван Петров");
        assertThat(createdUser.getEmail()).isEqualTo("ivan@example.com");

        // Проверяем, что пользователь сохранился в БД
        assertThat(userRepository.findById(createdUser.getId())).isPresent();
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailIsDuplicate() {

        UserDto userDto1 = UserDto.builder()
                .name("Иван Петров")
                .email("duplicate@example.com")
                .build();

        UserDto userDto2 = UserDto.builder()
                .name("Петр Иванов")
                .email("duplicate@example.com")
                .build();

        userService.createUser(userDto1);

        assertThatThrownBy(() -> userService.createUser(userDto2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("уже используется");
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        UserDto userDto = UserDto.builder()
                .name("Мария Сидорова")
                .email("maria@example.com")
                .build();

        UserDto savedUser = userService.createUser(userDto);

        UserDto foundUser = userService.getUserById(savedUser.getId());

        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.getName()).isEqualTo("Мария Сидорова");
        assertThat(foundUser.getEmail()).isEqualTo("maria@example.com");
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserDoesNotExist() {
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void updateUser_ShouldUpdateName_WhenOnlyNameProvided() {

        UserDto userDto = UserDto.builder()
                .name("Алексей Алексеев")
                .email("alexey@example.com")
                .build();

        UserDto savedUser = userService.createUser(userDto);

        Map<String, Object> updates = Map.of("name", "Алексей Обновленный");
        UserDto updatedUser = userService.updateUser(savedUser.getId(), updates);

        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(updatedUser.getName()).isEqualTo("Алексей Обновленный");
        assertThat(updatedUser.getEmail()).isEqualTo("alexey@example.com");
    }

    @Test
    void updateUser_ShouldUpdateEmail_WhenOnlyEmailProvided() {
        // Подготовка
        UserDto userDto = UserDto.builder()
                .name("Елена Смирнова")
                .email("elena@example.com")
                .build();

        UserDto savedUser = userService.createUser(userDto);

        Map<String, Object> updates = Map.of("email", "elena.new@example.com");
        UserDto updatedUser = userService.updateUser(savedUser.getId(), updates);

        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(updatedUser.getName()).isEqualTo("Елена Смирнова");
        assertThat(updatedUser.getEmail()).isEqualTo("elena.new@example.com");
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {

        UserDto user1 = UserDto.builder().name("User 1").email("user1@test.com").build();
        UserDto user2 = UserDto.builder().name("User 2").email("user2@test.com").build();
        UserDto user3 = UserDto.builder().name("User 3").email("user3@test.com").build();

        userService.createUser(user1);
        userService.createUser(user2);
        userService.createUser(user3);

        List<UserDto> allUsers = userService.getAllUsers();

        assertThat(allUsers).hasSize(3);
        assertThat(allUsers).extracting(UserDto::getName)
                .containsExactlyInAnyOrder("User 1", "User 2", "User 3");
    }

    @Test
    void deleteUser_ShouldRemoveUser_WhenUserExists() {

        UserDto userDto = UserDto.builder()
                .name("Для удаления")
                .email("delete@test.com")
                .build();

        UserDto savedUser = userService.createUser(userDto);

        userService.deleteUser(savedUser.getId());

        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
        assertThatThrownBy(() -> userService.getUserById(savedUser.getId()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserDoesNotExist() {
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("не найден");
    }
}
