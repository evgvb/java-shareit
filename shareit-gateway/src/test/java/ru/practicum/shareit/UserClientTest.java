package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserClientTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private UserClient userClient;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.uriTemplateHandler(any(DefaultUriBuilderFactory.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.requestFactory(any(Supplier.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        userClient = new UserClient("http://localhost:8080", restTemplateBuilder);

        // Заменяем rest в BaseClient на наш мок через рефлексию
        try {
            java.lang.reflect.Field restField = ru.practicum.shareit.client.BaseClient.class.getDeclaredField("rest");
            restField.setAccessible(true);
            restField.set(userClient, restTemplate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void createUser_whenValidData_thenSuccess() {
        UserDto userDto = UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Created", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> response = userClient.createUser(userDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Created", response.getBody());
    }

    @Test
    void updateUser_whenValidData_thenSuccess() {
        Long userId = 1L;
        Map<String, Object> updates = Map.of("name", "New Name");

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Updated", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> response = userClient.updateUser(userId, updates);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody());
    }

    @Test
    void getUserById_whenValidData_thenSuccess() {
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("User", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> response = userClient.getUserById(userId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User", response.getBody());
    }

    @Test
    void getAllUsers_whenCalled_thenSuccess() {
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("All Users", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> response = userClient.getAllUsers();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("All Users", response.getBody());
    }

    @Test
    void deleteUser_whenValidData_thenSuccess() {
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Deleted", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> response = userClient.deleteUser(userId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Deleted", response.getBody());
    }
}