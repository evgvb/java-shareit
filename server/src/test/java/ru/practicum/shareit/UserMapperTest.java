package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void toUserDto_ShouldConvertUserToDto() {
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        UserDto dto = UserMapper.toUserDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("John Doe");
        assertThat(dto.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void toUserDto_ShouldReturnNull_WhenUserIsNull() {
        assertThat(UserMapper.toUserDto(null)).isNull();
    }

    @Test
    void toUser_ShouldConvertDtoToUser() {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        User user = UserMapper.toUser(dto);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void toUser_ShouldReturnNull_WhenDtoIsNull() {
        assertThat(UserMapper.toUser(null)).isNull();
    }

    @Test
    void updateFromMap_ShouldUpdateName_WhenNameProvided() {
        User user = User.builder()
                .id(1L)
                .name("Old Name")
                .email("old@example.com")
                .build();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "New Name");

        User updatedUser = UserMapper.updateFromMap(user, updates);

        assertThat(updatedUser.getName()).isEqualTo("New Name");
        assertThat(updatedUser.getEmail()).isEqualTo("old@example.com");
    }

    @Test
    void updateFromMap_ShouldUpdateEmail_WhenEmailProvided() {
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("old@example.com")
                .build();

        Map<String, Object> updates = new HashMap<>();
        updates.put("email", "new@example.com");

        User updatedUser = UserMapper.updateFromMap(user, updates);

        assertThat(updatedUser.getName()).isEqualTo("John Doe");
        assertThat(updatedUser.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void updateFromMap_ShouldIgnoreBlankValues() {
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "");
        updates.put("email", "   ");

        User updatedUser = UserMapper.updateFromMap(user, updates);

        assertThat(updatedUser.getName()).isEqualTo("John Doe");
        assertThat(updatedUser.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void updateFromMap_ShouldReturnOriginalUser_WhenUpdatesIsEmpty() {
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        User updatedUser = UserMapper.updateFromMap(user, new HashMap<>());

        assertThat(updatedUser).isSameAs(user);
    }

    @Test
    void updateFromMap_ShouldReturnOriginalUser_WhenUpdatesIsNull() {
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        User updatedUser = UserMapper.updateFromMap(user, null);

        assertThat(updatedUser).isSameAs(user);
    }
}