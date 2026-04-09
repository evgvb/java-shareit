package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.info("Создание нового пользователя: {}", userDto.getEmail());

        verificationUserMail(userDto.getEmail());

        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);

        log.info("Пользователь создан с ID: {}", savedUser.getId());
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UpdateUserDto updates) {
        log.info("Обновление пользователя с ID: {}", userId);

        User existingUser = findUserById(userId);

        if (updates.getEmail() != null) {
            String newEmail = updates.getEmail(); // Не меняем регистр
            String oldEmail = existingUser.getEmail();

            if (!oldEmail.equalsIgnoreCase(newEmail)) {
                verificationUserMail(newEmail);
            }
            existingUser.setEmail(newEmail); // Сохраняем оригинальный регистр
        }

        if (updates.getName() != null && !updates.getName().isBlank()) {
            existingUser.setName(updates.getName());
        }

        User savedUser = userRepository.save(existingUser);

        log.info("Пользователь с ID {} обновлен", userId);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        log.info("Получение пользователя с ID: {}", userId);

        User user = findUserById(userId);

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
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с ID: {}", userId);

        findUserById(userId);

        userRepository.deleteById(userId);
        log.info("Пользователь с ID {} удален", userId);
    }

    private void verificationUserMail(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email " + email + " уже используется другим пользователем");
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));
    }
}