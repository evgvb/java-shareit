package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("Создание нового пользователя: {}", userDto.getEmail());

        verificationUserMail(userDto.getEmail().toLowerCase());

        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);

        log.info("Пользователь создан с ID: {}", savedUser.getId());
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Обновление пользователя с ID: {}", userId);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));

        if (userDto.getEmail() != null) {
            verificationUserMail(userDto.getEmail().toLowerCase());
        }

        User updatedUser = UserMapper.updateUserFromDto(existingUser, userDto);
        User savedUser = userRepository.update(updatedUser);

        log.info("Пользователь с ID {} обновлен", userId);
        return UserMapper.toUserDto(savedUser);
    }

    // Частичное обновление пользователя. Обновляет только полученные поля
    public UserDto partialUpdateUser(Long userId, UserUpdateDto updateDto) {
        log.info("Обновление пользователя с ID: {}", userId);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));

        if (updateDto.getEmail() != null && !updateDto.getEmail().isBlank()) {
            verificationUserMail(updateDto.getEmail().toLowerCase());
        }

        User updatedUser = UserMapper.updateUserFromDto(existingUser, updateDto);
        User savedUser = userRepository.update(updatedUser);

        log.info("Пользователь с ID {} обновлен", userId);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        log.info("Получение пользователя с ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));

        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Получение списка всех пользователей");

        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("Пользователь с ID " + userId + " не найден");
        }

        userRepository.deleteById(userId);
        log.info("Пользователь с ID {} удален", userId);
    }

    private void verificationUserMail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email " + email + " уже используется другим пользователем");
        }
    }
}