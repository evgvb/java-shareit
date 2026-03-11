package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.service.UserService;

import java.util.NoSuchElementException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestController testController() {
            return new TestController();
        }
    }

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
    void handleValidation_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/test/validation-error")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void handleIllegalArgument_ShouldReturn409() throws Exception {
        mockMvc.perform(get("/test/illegal-argument")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ошибка бизнес-логики"))
                .andExpect(jsonPath("$.message").value("Illegal argument"));
    }

    @Test
    void handleAllExceptions_ShouldReturn500() throws Exception {
        mockMvc.perform(get("/test/generic-error")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Внутренняя ошибка сервера"))
                .andExpect(jsonPath("$.message").value("Generic error"));
    }
}