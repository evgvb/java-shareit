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
import static org.mockito.ArgumentMatchers.anyString;
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
                .email("john@email.com")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@email.com")
                .build();
    }

    @Test
    void createUser_ShouldSaveAndReturnUser() {
        when(userRepository.existsByEmailIgnoreCase("john@email.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.createUser(userDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@email.com");

        verify(userRepository, times(1)).existsByEmailIgnoreCase("john@email.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmailIgnoreCase("john@email.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("уже используется");

        verify(userRepository, times(1)).existsByEmailIgnoreCase("john@email.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_ShouldPreserveEmailCase() {
        UserDto userDtoWithCase = UserDto.builder()
                .name("John Doe")
                .email("JOHN@email.COM")
                .build();

        User userWithCase = User.builder()
                .id(1L)
                .name("John Doe")
                .email("JOHN@email.COM")
                .build();

        when(userRepository.existsByEmailIgnoreCase("JOHN@email.COM")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(userWithCase);

        UserDto result = userService.createUser(userDtoWithCase);

        assertThat(result.getEmail()).isEqualTo("JOHN@email.COM");
        verify(userRepository).existsByEmailIgnoreCase("JOHN@email.COM");
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
        assertThat(result.getEmail()).isEqualTo("john@email.com");

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRepository, never()).existsByEmailIgnoreCase(anyString());
    }

    @Test
    void updateUser_ShouldUpdateEmail_WhenNewEmailIsUnique() {
        UpdateUserDto updates = UpdateUserDto.builder()
                .email("new@email.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCase("new@email.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserDto result = userService.updateUser(1L, updates);

        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("new@email.com");

        verify(userRepository, times(1)).existsByEmailIgnoreCase("new@email.com");
    }

    @Test
    void updateUser_ShouldNotCheckEmailUniqueness_WhenEmailNotChanged() {
        UpdateUserDto updates = UpdateUserDto.builder()
                .email("john@email.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.updateUser(1L, updates);

        verify(userRepository, never()).existsByEmailIgnoreCase(anyString());
    }

    @Test
    void updateUser_ShouldThrowException_WhenEmailAlreadyExists() {
        UpdateUserDto updates = UpdateUserDto.builder()
                .email("existing@email.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCase("existing@email.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, updates))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("уже используется");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_ShouldPreserveEmailCase_WhenUpdatingEmail() {
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@email.com")
                .build();

        UpdateUserDto updates = UpdateUserDto.builder()
                .email("JOHN.NEW@email.COM")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCase("JOHN.NEW@email.COM")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserDto result = userService.updateUser(1L, updates);

        assertThat(result.getEmail()).isEqualTo("JOHN.NEW@email.COM");
        verify(userRepository).existsByEmailIgnoreCase("JOHN.NEW@email.COM");
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(999L, UpdateUserDto.builder().name("new").build()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("не найден");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_ShouldThrowException_WhenNotExists() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void getAllUsers_ShouldReturnList() {
        User user2 = User.builder().id(2L).name("Jane").email("jane@email.com").build();
        List<User> users = Arrays.asList(user, user2);

        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserDto::getId).containsExactly(1L, 2L);

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).isEmpty();
    }

    @Test
    void deleteUser_ShouldDelete_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("не найден");

        verify(userRepository, never()).deleteById(anyLong());
    }
}