package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommentRepositoryTest extends RepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TestEntityManager em;

    private User author;
    private User owner;
    private Item item;
    private Comment comment;

    @BeforeEach
    void setUp() {
        author = User.builder()
                .name("Author")
                .email("author@email.com")
                .build();

        owner = User.builder()
                .name("Owner")
                .email("owner@email.com")
                .build();

        em.persist(author);
        em.persist(owner);

        item = Item.builder()
                .name("Item")
                .description("Desc")
                .available(true)
                .owner(owner)
                .build();
        em.persist(item);

        comment = Comment.builder()
                .text("Great item!")
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void findAllByItemId_shouldReturnComments() {
        em.persist(comment);

        List<Comment> comments = commentRepository.findAllByItemId(item.getId());

        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getText()).isEqualTo("Great item!");
        assertThat(comments.get(0).getAuthor().getName()).isEqualTo("Author");
    }

    @Test
    void findAllByItemIdIn_shouldReturnCommentsForMultipleItems() {
        em.persist(comment);

        Item item2 = Item.builder()
                .name("Item 2")
                .description("Desc 2")
                .available(true)
                .owner(owner)
                .build();
        em.persist(item2);

        Comment comment2 = Comment.builder()
                .text("Good item too")
                .item(item2)
                .author(author)
                .created(LocalDateTime.now())
                .build();
        em.persist(comment2);

        List<Comment> comments = commentRepository.findAllByItemIdIn(
                List.of(item.getId(), item2.getId()));

        assertThat(comments).hasSize(2);
    }

    @Test
    void saveComment_shouldSetId() {
        Comment saved = commentRepository.save(comment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getText()).isEqualTo("Great item!");
        assertThat(saved.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(saved.getItem().getId()).isEqualTo(item.getId());
    }
}
