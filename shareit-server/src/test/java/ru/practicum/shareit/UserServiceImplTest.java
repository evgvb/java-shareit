package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();
    }

    @Test
    void updateUser_ShouldUpdateNameOnly() {
        UpdateUserDto updates = UpdateUserDto.builder()
                .name("Jane Doe")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserDto result = userService.updateUser(1L, updates);

        assertThat(result.getName()).isEqualTo("Jane Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateUser_ShouldUpdateEmail_WhenNewEmailIsUnique() {
        UpdateUserDto updates = UpdateUserDto.builder()
                .email("new@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserDto result = userService.updateUser(1L, updates);

        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("new@example.com");

        verify(userRepository, times(1)).existsByEmail("new@example.com");
    }

    @Test
    void updateUser_ShouldNotCheckEmailUniqueness_WhenEmailNotChanged() {
        UpdateUserDto updates = UpdateUserDto.builder()
                .email("john@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.updateUser(1L, updates);

        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateUser_ShouldThrowException_WhenEmailAlreadyExists() {
        UpdateUserDto updates = UpdateUserDto.builder()
                .email("existing@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, updates))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("уже используется");

        verify(userRepository, never()).save(any(User.class));
    }
}