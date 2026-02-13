package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import java.util.List;
import java.util.Map;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto updateUser(Long userId, Map<String, Object> updates);

    UserDto getUserById(Long userId);

    List<UserDto> getAllUsers();

    void deleteUser(Long userId);
}