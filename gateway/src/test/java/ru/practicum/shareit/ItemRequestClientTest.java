package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;

import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestClientTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    @Captor
    private ArgumentCaptor<HttpEntity<CreateItemRequestDto>> httpEntityCaptor;

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    @Captor
    private ArgumentCaptor<HttpMethod> methodCaptor;

    @Captor
    private ArgumentCaptor<Class<Object>> responseTypeCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> parametersCaptor;

    private ItemRequestClient itemRequestClient;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.uriTemplateHandler(any(DefaultUriBuilderFactory.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.requestFactory(any(Supplier.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        itemRequestClient = new ItemRequestClient("http://localhost:9090", restTemplateBuilder);
    }

    @Test
    void createItemRequest_whenValidData_thenSuccess() {
        // Given
        CreateItemRequestDto createDto = CreateItemRequestDto.builder()
                .description("Нужна дрель")
                .build();
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Created", HttpStatus.OK);

        // Настраиваем мок с правильными аргументами
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemRequestClient.createItemRequest(createDto, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Created", response.getBody());

        verify(restTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void createItemRequest_whenValidData_thenVerifyCorrectParameters() {
        // Given
        CreateItemRequestDto createDto = CreateItemRequestDto.builder()
                .description("Нужна дрель")
                .build();
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Created", HttpStatus.OK);

        when(restTemplate.exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                httpEntityCaptor.capture(),
                responseTypeCaptor.capture()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemRequestClient.createItemRequest(createDto, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверяем URL - должен быть пустым, так как базовый URL уже содержит /requests
        assertEquals("", urlCaptor.getValue());

        // Проверяем HTTP метод
        assertEquals(HttpMethod.POST, methodCaptor.getValue());

        // Проверяем тип ответа
        assertEquals(Object.class, responseTypeCaptor.getValue());

        // Проверяем заголовки
        HttpEntity<CreateItemRequestDto> capturedEntity = httpEntityCaptor.getValue();
        assertNotNull(capturedEntity);

        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals(MediaType.APPLICATION_JSON, headers.getAccept().get(0));
        assertEquals("1", headers.getFirst("X-Sharer-User-Id"));

        // Проверяем тело запроса
        CreateItemRequestDto capturedBody = capturedEntity.getBody();
        assertNotNull(capturedBody);
        assertEquals("Нужна дрель", capturedBody.getDescription());
    }

    @Test
    void getUserItemRequests_whenValidData_thenSuccess() {
        // Given
        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("User Requests", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class),
                any(Map.class)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemRequestClient.getUserItemRequests(userId, from, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User Requests", response.getBody());
    }

    @Test
    void getUserItemRequests_whenValidData_thenVerifyCorrectParameters() {
        // Given
        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("User Requests", HttpStatus.OK);

        when(restTemplate.exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                httpEntityCaptor.capture(),
                responseTypeCaptor.capture(),
                parametersCaptor.capture()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemRequestClient.getUserItemRequests(userId, from, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверяем URL
        assertEquals("?from={from}&size={size}", urlCaptor.getValue());

        // Проверяем HTTP метод
        assertEquals(HttpMethod.GET, methodCaptor.getValue());

        // Проверяем тип ответа
        assertEquals(Object.class, responseTypeCaptor.getValue());

        // Проверяем параметры
        Map<String, Object> parameters = parametersCaptor.getValue();
        assertEquals(from, parameters.get("from"));
        assertEquals(size, parameters.get("size"));

        // Проверяем заголовки
        HttpEntity<?> capturedEntity = httpEntityCaptor.getValue();
        assertNotNull(capturedEntity);

        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals(MediaType.APPLICATION_JSON, headers.getAccept().get(0));
        assertEquals("1", headers.getFirst("X-Sharer-User-Id"));
    }

    @Test
    void getAllItemRequests_whenValidData_thenSuccess() {
        // Given
        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("All Requests", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class),
                any(Map.class)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemRequestClient.getAllItemRequests(userId, from, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("All Requests", response.getBody());
    }

    @Test
    void getAllItemRequests_whenValidData_thenVerifyCorrectParameters() {
        // Given
        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("All Requests", HttpStatus.OK);

        when(restTemplate.exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                httpEntityCaptor.capture(),
                responseTypeCaptor.capture(),
                parametersCaptor.capture()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemRequestClient.getAllItemRequests(userId, from, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверяем URL
        assertEquals("/all?from={from}&size={size}", urlCaptor.getValue());

        // Проверяем HTTP метод
        assertEquals(HttpMethod.GET, methodCaptor.getValue());

        // Проверяем параметры
        Map<String, Object> parameters = parametersCaptor.getValue();
        assertEquals(from, parameters.get("from"));
        assertEquals(size, parameters.get("size"));

        // Проверяем заголовки
        HttpEntity<?> capturedEntity = httpEntityCaptor.getValue();
        assertNotNull(capturedEntity);

        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals("1", headers.getFirst("X-Sharer-User-Id"));
    }

    @Test
    void getItemRequestById_whenValidData_thenSuccess() {
        // Given
        Long requestId = 1L;
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Request", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemRequestClient.getItemRequestById(requestId, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Request", response.getBody());
    }

    @Test
    void getItemRequestById_whenValidData_thenVerifyCorrectParameters() {
        // Given
        Long requestId = 1L;
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Request", HttpStatus.OK);

        when(restTemplate.exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                httpEntityCaptor.capture(),
                responseTypeCaptor.capture()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemRequestClient.getItemRequestById(requestId, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверяем URL
        assertEquals("/" + requestId, urlCaptor.getValue());

        // Проверяем HTTP метод
        assertEquals(HttpMethod.GET, methodCaptor.getValue());

        // Проверяем заголовки
        HttpEntity<?> capturedEntity = httpEntityCaptor.getValue();
        assertNotNull(capturedEntity);

        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals("1", headers.getFirst("X-Sharer-User-Id"));
    }

    @Test
    void createItemRequest_whenServerError_thenHandleException() {
        // Given
        CreateItemRequestDto createDto = CreateItemRequestDto.builder()
                .description("Нужна дрель")
                .build();
        Long userId = 1L;

        org.springframework.web.client.HttpServerErrorException exception =
                new org.springframework.web.client.HttpServerErrorException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Server Error");

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenThrow(exception);

        // When
        ResponseEntity<Object> response = itemRequestClient.createItemRequest(createDto, userId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}