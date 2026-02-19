package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(UserDto.Create.class)
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        log.info("POST /users - создание пользователя: {}", userDto.getEmail());
        return userService.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    @Validated(UserDto.Update.class)
    public UserDto updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> updates) {
        log.info("PATCH /users/{} - обновление пользователя", userId);
        return userService.updateUser(userId, updates);
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        log.info("GET /users/{} - получение пользователя по ID", userId);
        return userService.getUserById(userId);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("GET /users - получение всех пользователей");
        return userService.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("DELETE /users/{} - удаление пользователя", userId);
        userService.deleteUser(userId);
    }
}