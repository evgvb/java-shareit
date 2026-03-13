package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends RepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("user1")
                .email("user1@email.com")
                .build();
    }

    @Test
    void existsByEmailIgnoreCase_whenEmailExists_shouldReturnTrue() {
        em.persist(user);

        boolean exists = userRepository.existsByEmailIgnoreCase("USER1@EMAIL.COM");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmailIgnoreCase_whenEmailNotExists_shouldReturnFalse() {
        boolean exists = userRepository.existsByEmailIgnoreCase("none@EMAIL.com");

        assertThat(exists).isFalse();
    }

    @Test
    void saveUser_shouldSetId() {
        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("user1");
        assertThat(saved.getEmail()).isEqualTo("user1@email.com");
    }

    @Test
    void findById_whenUserExists_shouldReturnUser() {
        em.persist(user);

        User found = userRepository.findById(user.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        em.persist(user);

        User user2 = User.builder()
                .name("user2")
                .email("user2@email.com")
                .build();
        em.persist(user2);

        assertThat(userRepository.findAll()).hasSize(2);
    }

    @Test
    void deleteById_shouldRemoveUser() {
        em.persist(user);

        userRepository.deleteById(user.getId());

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }
}