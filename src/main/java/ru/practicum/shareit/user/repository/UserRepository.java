package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, Long> emails = new HashMap<>();
    private Long idCounter = 1L;

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idCounter++);
        }

        String email = user.getEmail().toLowerCase();
        if (emails.containsKey(email) && !emails.get(email).equals(user.getId())) {
            throw new IllegalArgumentException("Email уже используется другим пользователем");
        }

        users.put(user.getId(), user);
        emails.put(email, user.getId());
        return user;
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public Optional<User> findByEmail(String email) {
        Long userId = emails.get(email.toLowerCase());
        if (userId != null) {
            return Optional.ofNullable(users.get(userId));
        }
        return Optional.empty();
    }

    public boolean existsByEmail(String email) {
        return emails.containsKey(email.toLowerCase());
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public User update(User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            throw new NoSuchElementException("Пользователь не найден");
        }

        User existingUser = users.get(user.getId());
        String oldEmail = existingUser.getEmail().toLowerCase();
        String newEmail = user.getEmail().toLowerCase();

        if (!oldEmail.equals(newEmail) && emails.containsKey(newEmail)) {
            throw new IllegalArgumentException("Email уже используется другим пользователем");
        }

        emails.remove(oldEmail);
        emails.put(newEmail, user.getId());

        users.put(user.getId(), user);
        return user;
    }

    public void deleteById(Long id) {
        User user = users.remove(id);
        if (user != null) {
            emails.remove(user.getEmail().toLowerCase());
        }
    }

    public boolean existsById(Long id) {
        return users.containsKey(id);
    }
}