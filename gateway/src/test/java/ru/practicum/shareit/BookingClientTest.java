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
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Captor
    private ArgumentCaptor<HttpEntity<BookingDto>> httpEntityCaptor;

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    @Captor
    private ArgumentCaptor<HttpMethod> methodCaptor;

    @Captor
    private ArgumentCaptor<Class<Object>> responseTypeCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> parametersCaptor;

    private BookingClient bookingClient;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.uriTemplateHandler(any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.requestFactory(any(Supplier.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        bookingClient = new BookingClient("http://localhost:9090", restTemplateBuilder);
    }

    @Test
    void createBooking_whenValidData_thenSuccess() {
        // Given
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
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
        ResponseEntity<Object> response = bookingClient.createBooking(bookingDto, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Created", response.getBody());

        verify(restTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void createBooking_whenValidData_thenVerifyCorrectParameters() {
        // Given
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
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
        ResponseEntity<Object> response = bookingClient.createBooking(bookingDto, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверяем URL - должен быть пустым, так как базовый URL уже содержит /bookings
        assertEquals("", urlCaptor.getValue());

        // Проверяем HTTP метод
        assertEquals(HttpMethod.POST, methodCaptor.getValue());

        // Проверяем тип ответа
        assertEquals(Object.class, responseTypeCaptor.getValue());

        // Проверяем заголовки
        HttpEntity<BookingDto> capturedEntity = httpEntityCaptor.getValue();
        assertNotNull(capturedEntity);

        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals(MediaType.APPLICATION_JSON, headers.getAccept().get(0));
        assertEquals("1", headers.getFirst("X-Sharer-User-Id"));

        // Проверяем тело запроса
        BookingDto capturedBody = capturedEntity.getBody();
        assertNotNull(capturedBody);
        assertEquals(bookingDto.getItemId(), capturedBody.getItemId());
        assertEquals(bookingDto.getStart(), capturedBody.getStart());
        assertEquals(bookingDto.getEnd(), capturedBody.getEnd());
    }

    @Test
    void approveBooking_whenValidData_thenSuccess() {
        // Given
        Long bookingId = 1L;
        Boolean approved = true;
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Approved", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class),
                any(Map.class)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = bookingClient.approveBooking(bookingId, approved, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Approved", response.getBody());
    }

    @Test
    void approveBooking_whenValidData_thenVerifyCorrectParameters() {
        // Given
        Long bookingId = 1L;
        Boolean approved = true;
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Approved", HttpStatus.OK);

        when(restTemplate.exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                httpEntityCaptor.capture(),
                responseTypeCaptor.capture(),
                parametersCaptor.capture()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = bookingClient.approveBooking(bookingId, approved, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверяем URL
        assertEquals("/" + bookingId + "?approved={approved}", urlCaptor.getValue());

        // Проверяем HTTP метод
        assertEquals(HttpMethod.PATCH, methodCaptor.getValue());

        // Проверяем параметры
        Map<String, Object> parameters = parametersCaptor.getValue();
        assertEquals(approved, parameters.get("approved"));

        // Проверяем заголовки
        HttpEntity<?> capturedEntity = httpEntityCaptor.getValue();
        assertNotNull(capturedEntity);

        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals(MediaType.APPLICATION_JSON, headers.getAccept().get(0));
        assertEquals("1", headers.getFirst("X-Sharer-User-Id"));

        // Для PATCH запроса тело должно быть null
        assertNull(capturedEntity.getBody());
    }

    @Test
    void getBookingById_whenValidData_thenSuccess() {
        // Given
        Long bookingId = 1L;
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Booking", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = bookingClient.getBookingById(bookingId, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Booking", response.getBody());
    }

    @Test
    void getBookingById_whenValidData_thenVerifyCorrectParameters() {
        // Given
        Long bookingId = 1L;
        Long userId = 1L;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Booking", HttpStatus.OK);

        when(restTemplate.exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                httpEntityCaptor.capture(),
                responseTypeCaptor.capture()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = bookingClient.getBookingById(bookingId, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверяем URL
        assertEquals("/" + bookingId, urlCaptor.getValue());

        // Проверяем HTTP метод
        assertEquals(HttpMethod.GET, methodCaptor.getValue());

        // Проверяем заголовки
        HttpEntity<?> capturedEntity = httpEntityCaptor.getValue();
        assertNotNull(capturedEntity);

        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals(MediaType.APPLICATION_JSON, headers.getAccept().get(0));
        assertEquals("1", headers.getFirst("X-Sharer-User-Id"));
    }

    @Test
    void getUserBookings_whenValidData_thenSuccess() {
        // Given
        Long userId = 1L;
        BookingState state = BookingState.ALL;
        Integer from = 0;
        Integer size = 10;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("User Bookings", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class),
                any(Map.class)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = bookingClient.getUserBookings(userId, state, from, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User Bookings", response.getBody());
    }

    @Test
    void getUserBookings_whenValidData_thenVerifyCorrectParameters() {
        // Given
        Long userId = 1L;
        BookingState state = BookingState.ALL;
        Integer from = 0;
        Integer size = 10;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("User Bookings", HttpStatus.OK);

        when(restTemplate.exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                httpEntityCaptor.capture(),
                responseTypeCaptor.capture(),
                parametersCaptor.capture()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = bookingClient.getUserBookings(userId, state, from, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверяем URL
        assertEquals("?state={state}&from={from}&size={size}", urlCaptor.getValue());

        // Проверяем HTTP метод
        assertEquals(HttpMethod.GET, methodCaptor.getValue());

        // Проверяем параметры
        Map<String, Object> parameters = parametersCaptor.getValue();
        assertEquals(state.name(), parameters.get("state"));
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
    void getOwnerBookings_whenValidData_thenSuccess() {
        // Given
        Long userId = 1L;
        BookingState state = BookingState.ALL;
        Integer from = 0;
        Integer size = 10;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Owner Bookings", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class),
                any(Map.class)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = bookingClient.getOwnerBookings(userId, state, from, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Owner Bookings", response.getBody());
    }

    @Test
    void getOwnerBookings_whenValidData_thenVerifyCorrectParameters() {
        // Given
        Long userId = 1L;
        BookingState state = BookingState.ALL;
        Integer from = 0;
        Integer size = 10;

        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("Owner Bookings", HttpStatus.OK);

        when(restTemplate.exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                httpEntityCaptor.capture(),
                responseTypeCaptor.capture(),
                parametersCaptor.capture()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<Object> response = bookingClient.getOwnerBookings(userId, state, from, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверяем URL
        assertEquals("/owner?state={state}&from={from}&size={size}", urlCaptor.getValue());

        // Проверяем HTTP метод
        assertEquals(HttpMethod.GET, methodCaptor.getValue());

        // Проверяем параметры
        Map<String, Object> parameters = parametersCaptor.getValue();
        assertEquals(state.name(), parameters.get("state"));
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
    void createBooking_whenServerError_thenHandleException() {
        // Given
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
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
        ResponseEntity<Object> response = bookingClient.createBooking(bookingDto, userId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}