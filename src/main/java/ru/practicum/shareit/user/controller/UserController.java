package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

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

    @PutMapping("/{userId}")
    @Validated(UserDto.Update.class)
    public UserDto updateUser(@PathVariable Long userId, @Valid @RequestBody UserDto userDto) {
        log.info("PUT /users/{} - полное обновление пользователя", userId);
        return userService.updateUser(userId, userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto partialUpdateUser(@PathVariable Long userId, @Valid @RequestBody UserUpdateDto updateDto) {
        log.info("PATCH /users/{} - обновление пользователя", userId);
        // Приведение к UserServiceImpl для доступа к новому методу
        if (userService instanceof ru.practicum.shareit.user.service.UserServiceImpl) {
            return ((ru.practicum.shareit.user.service.UserServiceImpl) userService)
                    .partialUpdateUser(userId, updateDto);
        }
        throw new UnsupportedOperationException("Метод не поддерживается");
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