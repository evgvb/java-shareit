package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest extends BaseControllerTest {

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("user1")
                .email("user1@email.com")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("user1")
                .email("user1@email.com")
                .build();
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        when(userService.createUser(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("user1"))
                .andExpect(jsonPath("$.email").value("user1@email.com"));

        verify(userService, times(1)).createUser(any(UserDto.class));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        Map<String, Object> updates = Map.of("name", "user2");
        UserDto updatedDto = UserDto.builder()
                .id(1L)
                .name("user2")
                .email("user2@email.com")
                .build();

        when(userService.updateUser(eq(1L), any(Map.class))).thenReturn(updatedDto);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("user2"))
                .andExpect(jsonPath("$.email").value("user2@email.com"));

        verify(userService, times(1)).updateUser(eq(1L), any(Map.class));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("user1"));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getUserById_ShouldReturn404_WhenUserNotFound() throws Exception {
        when(userService.getUserById(999L)).thenThrow(new NoSuchElementException("Пользователь не найден"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(999L);
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        UserDto user2 = UserDto.builder().id(2L).name("user2").email("user2@email.com").build();
        List<UserDto> users = Arrays.asList(userDto, user2);

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void deleteUser_ShouldReturnNoContent_WhenUserExists() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }
}