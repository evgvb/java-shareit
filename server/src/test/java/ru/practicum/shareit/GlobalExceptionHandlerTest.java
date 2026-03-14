package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.GlobalExceptionHandler;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.service.UserService;

import java.util.NoSuchElementException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {
        GlobalExceptionHandlerTest.TestController.class,
        GlobalExceptionHandler.class
})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private ItemRequestService itemRequestService;

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/not-found")
        public String throwNotFound() {
            throw new NoSuchElementException("Resource not found");
        }

        @GetMapping("/validation-error")
        public String throwValidation() {
            throw new ValidationException("Validation failed");
        }

        @GetMapping("/illegal-argument")
        public String throwIllegalArgument() {
            throw new IllegalArgumentException("Illegal argument");
        }

        @GetMapping("/access-denied")
        public String throwAccessDenied() {
            throw new AccessDeniedException("Access denied");
        }

        @DeleteMapping("/access-denied")
        public String throwAccessDeniedDelete() {
            throw new AccessDeniedException("Access denied");
        }

        @PostMapping("/access-denied")
        public String throwAccessDeniedPost() {
            throw new AccessDeniedException("Access denied");
        }

        @PutMapping("/access-denied")
        public String throwAccessDeniedPut() {
            throw new AccessDeniedException("Access denied");
        }

        @PatchMapping("/access-denied")
        public String throwAccessDeniedPatch() {
            throw new AccessDeniedException("Access denied");
        }

        @GetMapping("/generic-error")
        public String throwGenericError() {
            throw new RuntimeException("Generic error");
        }
    }

    @Test
    void handleNotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/test/not-found")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Объект не найден"))
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @Test
    void handleValidationException_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/test/validation-error")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void handleIllegalArgumentException_ShouldReturn409() throws Exception {
        mockMvc.perform(get("/test/illegal-argument")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ошибка бизнес-логики"))
                .andExpect(jsonPath("$.message").value("Illegal argument"));
    }

    @Test
    void handleAccessDeniedException_OnGetRequest_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/test/access-denied")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Доступ запрещен"))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void handleAccessDeniedException_OnPostRequest_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/test/access-denied")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Доступ запрещен"))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void handleAccessDeniedException_OnPutRequest_ShouldReturn403() throws Exception {
        mockMvc.perform(put("/test/access-denied")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Доступ запрещен"))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void handleAccessDeniedException_OnPatchRequest_ShouldReturn403() throws Exception {
        mockMvc.perform(patch("/test/access-denied")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Доступ запрещен"))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void handleAccessDeniedException_OnDeleteRequest_ShouldReturn403() throws Exception {
        mockMvc.perform(delete("/test/access-denied")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Доступ запрещен"))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void handleGenericException_ShouldReturn500() throws Exception {
        mockMvc.perform(get("/test/generic-error")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Внутренняя ошибка сервера"))
                .andExpect(jsonPath("$.message").value("Generic error"));
    }
}