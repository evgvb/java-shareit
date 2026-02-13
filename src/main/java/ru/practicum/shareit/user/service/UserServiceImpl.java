package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
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
    public UserDto updateUser(Long userId, Map<String, Object> updates) {
        log.info("Обновление пользователя с ID: {}", userId);

        User existingUser = findUserById(userId);

        if (updates.containsKey("email") && updates.get("email") != null) {
            String newEmail = updates.get("email").toString().toLowerCase();
            String oldEmail = existingUser.getEmail().toLowerCase();

            if (!oldEmail.equals(newEmail)) {
                verificationUserMail(newEmail);
            }
        }

        User updatedUser = UserMapper.updateFromMap(existingUser, updates);
        User savedUser = userRepository.update(updatedUser);

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
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с ID: {}", userId);

        findUserById(userId);

        userRepository.deleteById(userId);
        log.info("Пользователь с ID {} удален", userId);
    }

    private void verificationUserMail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email " + email + " уже используется другим пользователем");
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));
    }
}