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
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemClientTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    @Captor
    private ArgumentCaptor<HttpEntity<ItemDto>> httpEntityCaptor;

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    @Captor
    private ArgumentCaptor<HttpMethod> methodCaptor;

    @Captor
    private ArgumentCaptor<Class<Object>> responseTypeCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> parametersCaptor;

    private ItemClient itemClient;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.uriTemplateHandler(any(DefaultUriBuilderFactory.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.requestFactory(any(Supplier.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        itemClient = new ItemClient("http://localhost:9090", restTemplateBuilder);
    }

    @Test
    void createItem_whenValidData_thenSuccess() {
        // Given
        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Created", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemClient.createItem(itemDto, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Created", response.getBody());

        verify(restTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void createItem_whenValidData_thenVerifyCorrectParameters() {
        // Given
        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
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
        ResponseEntity<Object> response = itemClient.createItem(itemDto, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверяем URL - должен быть пустым, так как базовый URL уже содержит /items
        assertEquals("", urlCaptor.getValue());

        // Проверяем HTTP метод
        assertEquals(HttpMethod.POST, methodCaptor.getValue());

        // Проверяем тип ответа
        assertEquals(Object.class, responseTypeCaptor.getValue());

        // Проверяем заголовки
        HttpEntity<ItemDto> capturedEntity = httpEntityCaptor.getValue();
        assertNotNull(capturedEntity);

        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals(MediaType.APPLICATION_JSON, headers.getAccept().get(0));
        assertEquals("1", headers.getFirst("X-Sharer-User-Id"));

        // Проверяем тело запроса
        ItemDto capturedBody = capturedEntity.getBody();
        assertNotNull(capturedBody);
        assertEquals("Дрель", capturedBody.getName());
        assertEquals("Аккумуляторная дрель", capturedBody.getDescription());
        assertTrue(capturedBody.getAvailable());
    }

    @Test
    void updateItem_whenValidData_thenSuccess() {
        // Given
        Long itemId = 1L;
        Map<String, Object> updates = Map.of("name", "Новое название");
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Updated", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemClient.updateItem(itemId, updates, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody());
    }

    @Test
    void updateItem_whenValidData_thenVerifyCorrectParameters() {
        // Given
        Long itemId = 1L;
        Map<String, Object> updates = Map.of("name", "Новое название");
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Updated", HttpStatus.OK);

        when(restTemplate.exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                httpEntityCaptor.capture(),
                responseTypeCaptor.capture()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemClient.updateItem(itemId, updates, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверяем URL
        assertEquals("/" + itemId, urlCaptor.getValue());

        // Проверяем HTTP метод
        assertEquals(HttpMethod.PATCH, methodCaptor.getValue());

        // Проверяем тип ответа
        assertEquals(Object.class, responseTypeCaptor.getValue());

        // Проверяем заголовки
        HttpEntity<?> capturedEntity = httpEntityCaptor.getValue();
        assertNotNull(capturedEntity);

        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals(MediaType.APPLICATION_JSON, headers.getAccept().get(0));
        assertEquals("1", headers.getFirst("X-Sharer-User-Id"));

        // Проверяем тело запроса
        Map<?, ?> capturedBody = (Map<?, ?>) capturedEntity.getBody();
        assertNotNull(capturedBody);
        assertEquals("Новое название", capturedBody.get("name"));
    }

    @Test
    void getItemById_whenValidData_thenSuccess() {
        // Given
        Long itemId = 1L;
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Item", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemClient.getItemById(itemId, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Item", response.getBody());
    }

    @Test
    void getItemById_whenValidData_thenVerifyCorrectParameters() {
        // Given
        Long itemId = 1L;
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Item", HttpStatus.OK);

        when(restTemplate.exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                httpEntityCaptor.capture(),
                responseTypeCaptor.capture()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemClient.getItemById(itemId, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверяем URL
        assertEquals("/" + itemId, urlCaptor.getValue());

        // Проверяем HTTP метод
        assertEquals(HttpMethod.GET, methodCaptor.getValue());

        // Проверяем тип ответа
        assertEquals(Object.class, responseTypeCaptor.getValue());

        // Проверяем заголовки
        HttpEntity<?> capturedEntity = httpEntityCaptor.getValue();
        assertNotNull(capturedEntity);

        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals(MediaType.APPLICATION_JSON, headers.getAccept().get(0));
        assertEquals("1", headers.getFirst("X-Sharer-User-Id"));
    }

    @Test
    void getAllItemsByOwner_whenValidData_thenSuccess() {
        // Given
        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Items", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class),
                any(Map.class)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemClient.getAllItemsByOwner(userId, from, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Items", response.getBody());
    }

    @Test
    void getAllItemsByOwner_whenValidData_thenVerifyCorrectParameters() {
        // Given
        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Items", HttpStatus.OK);

        when(restTemplate.exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                httpEntityCaptor.capture(),
                responseTypeCaptor.capture(),
                parametersCaptor.capture()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemClient.getAllItemsByOwner(userId, from, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверяем URL
        assertEquals("?from={from}&size={size}", urlCaptor.getValue());

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
    void searchItems_whenValidData_thenSuccess() {
        // Given
        String text = "дрель";
        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Search Results", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class),
                any(Map.class)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemClient.searchItems(text, userId, from, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Search Results", response.getBody());
    }

    @Test
    void searchItems_whenValidData_thenVerifyCorrectParameters() {
        // Given
        String text = "дрель";
        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Search Results", HttpStatus.OK);

        when(restTemplate.exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                httpEntityCaptor.capture(),
                responseTypeCaptor.capture(),
                parametersCaptor.capture()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemClient.searchItems(text, userId, from, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверяем URL
        assertEquals("/search?text={text}&from={from}&size={size}", urlCaptor.getValue());

        // Проверяем HTTP метод
        assertEquals(HttpMethod.GET, methodCaptor.getValue());

        // Проверяем параметры
        Map<String, Object> parameters = parametersCaptor.getValue();
        assertEquals(text, parameters.get("text"));
        assertEquals(from, parameters.get("from"));
        assertEquals(size, parameters.get("size"));

        // Проверяем заголовки
        HttpEntity<?> capturedEntity = httpEntityCaptor.getValue();
        assertNotNull(capturedEntity);

        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals("1", headers.getFirst("X-Sharer-User-Id"));
    }

    @Test
    void addComment_whenValidData_thenSuccess() {
        // Given
        Long itemId = 1L;
        CreateCommentDto commentDto = CreateCommentDto.builder()
                .text("Отличная вещь!")
                .build();
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Comment Added", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemClient.addComment(itemId, commentDto, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Comment Added", response.getBody());
    }

    @Test
    void addComment_whenValidData_thenVerifyCorrectParameters() {
        // Given
        Long itemId = 1L;
        CreateCommentDto commentDto = CreateCommentDto.builder()
                .text("Отличная вещь!")
                .build();
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Comment Added", HttpStatus.OK);

        when(restTemplate.exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                httpEntityCaptor.capture(),
                responseTypeCaptor.capture()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemClient.addComment(itemId, commentDto, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверяем URL
        assertEquals("/" + itemId + "/comment", urlCaptor.getValue());

        // Проверяем HTTP метод
        assertEquals(HttpMethod.POST, methodCaptor.getValue());

        // Проверяем тип ответа
        assertEquals(Object.class, responseTypeCaptor.getValue());

        // Проверяем заголовки
        HttpEntity<?> capturedEntity = httpEntityCaptor.getValue();
        assertNotNull(capturedEntity);

        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals(MediaType.APPLICATION_JSON, headers.getAccept().get(0));
        assertEquals("1", headers.getFirst("X-Sharer-User-Id"));
    }

    @Test
    void deleteItem_whenValidData_thenSuccess() {
        // Given
        Long itemId = 1L;
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Deleted", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemClient.deleteItem(itemId, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Deleted", response.getBody());
    }

    @Test
    void deleteItem_whenValidData_thenVerifyCorrectParameters() {
        // Given
        Long itemId = 1L;
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Deleted", HttpStatus.OK);

        when(restTemplate.exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                httpEntityCaptor.capture(),
                responseTypeCaptor.capture()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = itemClient.deleteItem(itemId, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверяем URL
        assertEquals("/" + itemId, urlCaptor.getValue());

        // Проверяем HTTP метод
        assertEquals(HttpMethod.DELETE, methodCaptor.getValue());

        // Проверяем заголовки
        HttpEntity<?> capturedEntity = httpEntityCaptor.getValue();
        assertNotNull(capturedEntity);

        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals(MediaType.APPLICATION_JSON, headers.getAccept().get(0));
        assertEquals("1", headers.getFirst("X-Sharer-User-Id"));
    }

    @Test
    void createItem_whenServerError_thenHandleException() {
        // Given
        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
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
        ResponseEntity<Object> response = itemClient.createItem(itemDto, userId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}