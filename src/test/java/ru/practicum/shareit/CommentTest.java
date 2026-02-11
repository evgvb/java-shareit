package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CommentTest {
    private CommentRepository commentRepository;
    private User author;
    private Item item;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        commentRepository = new CommentRepository();

        author = User.builder()
                .id(1L)
                .name("User 1")
                .email("user1@email.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Item 1")
                .description("item 1 описание")
                .available(true)
                .owner(author)
                .build();

        now = LocalDateTime.now();
    }

    // сохранение нового комментария
    @Test
    void save_shouldSaveNewCommentAndAssignId() {
        Comment comment = Comment.builder()
                .text("Коммент 1")
                .item(item)
                .author(author)
                .created(now)
                .build();

        Comment savedComment = commentRepository.save(comment);

        assertNotNull(savedComment.getId(), "Сохраненному комментарию должен быть присвоен ID");
        assertEquals("Коммент 1", savedComment.getText(), "Текст должен сохраниться");
        assertEquals(item.getId(), savedComment.getItem().getId(), "ID вещи должно сохраниться");
        assertEquals(author.getId(), savedComment.getAuthor().getId(), "ID автора должно сохраниться");
        assertEquals(now, savedComment.getCreated(), "Дата создания должна сохраниться");
    }

    // поиска комментария по ID
    @Test
    void findById_shouldReturnCommentWhenExists() {
        Comment comment = Comment.builder()
                .text("Коммент 1")
                .item(item)
                .author(author)
                .created(now)
                .build();
        Comment savedComment = commentRepository.save(comment);

        Optional<Comment> foundComment = commentRepository.findById(savedComment.getId());

        assertTrue(foundComment.isPresent(), "Комментарий должен быть найден");
        assertEquals(savedComment.getId(), foundComment.get().getId(), "ID должно совпадать");
        assertEquals("Коммент 1", foundComment.get().getText(), "Текст должен совпадать");
    }

    // поиск всех комментариев вещи
    @Test
    void findAllByItemId_shouldReturnAllCommentsForItem() {
        Item item2 = Item.builder()
                .id(2L)
                .name("Item 2")
                .owner(author)
                .build();


        Comment commentForItem1 = Comment.builder()
                .text("Комменt 1")
                .item(item)
                .author(author)
                .created(now)
                .build();

        Comment commentForItem2 = Comment.builder()
                .text("Коммент 2")
                .item(item2)
                .author(author)
                .created(now)
                .build();

        commentRepository.save(commentForItem1);
        commentRepository.save(commentForItem2);

        List<Comment> comments = commentRepository.findAllByItemId(item.getId());

        assertEquals(1, comments.size(), "Должен вернуться один комментарий для вещи");
        assertEquals(item.getId(), comments.get(0).getItem().getId(),
                "ID вещи должно совпадать");
    }

    // комментарии по списку id вещей.
    @Test
    void findAllByItemIds_shouldReturnCommentsForMultipleItems() {
        // Arrange - создаем вторую вещь
        Item item2 = Item.builder()
                .id(2L)
                .name("Молоток")
                .owner(author)
                .build();

        // Создаем несколько комментариев
        Comment comment1 = Comment.builder()
                .text("Комментарий 1 к item 1")
                .item(item)
                .author(author)
                .created(now)
                .build();

        Comment comment2 = Comment.builder()
                .text("Комментарий 2 к item 1")
                .item(item)
                .author(author)
                .created(now.plusHours(1))
                .build();

        Comment comment3 = Comment.builder()
                .text("Комментарий к item 2")
                .item(item2)
                .author(author)
                .created(now)
                .build();

        commentRepository.save(comment1);
        commentRepository.save(comment2);
        commentRepository.save(comment3);

        List<Long> itemIds = List.of(item.getId(), item2.getId());

        Map<Long, List<Comment>> commentsByItem = commentRepository.findAllByItemIds(itemIds);

        assertEquals(2, commentsByItem.size(), "Должны вернуться комментарии для 2 вещей");

        // комментарии для первой вещи
        List<Comment> commentsForItem1 = commentsByItem.get(item.getId());
        assertNotNull(commentsForItem1, "Для первой вещи должны быть комментарии");
        assertEquals(2, commentsForItem1.size(), "Для первой вещи должно быть 2 комментария");

        // комментарии для второй вещи
        List<Comment> commentsForItem2 = commentsByItem.get(item2.getId());
        assertNotNull(commentsForItem2, "Для второй вещи должны быть комментарии");
        assertEquals(1, commentsForItem2.size(), "Для второй вещи должен быть 1 комментарий");
    }

    // удаление комментария
    @Test
    void deleteById_shouldRemoveCommentFromRepository() {
        Comment comment = Comment.builder()
                .text("Коммент 1")
                .item(item)
                .author(author)
                .created(now)
                .build();
        Comment savedComment = commentRepository.save(comment);

        // комментарий существует
        assertTrue(commentRepository.findById(savedComment.getId()).isPresent(),
                "Комментарий должен существовать до удаления");

        commentRepository.deleteById(savedComment.getId());

        assertTrue(commentRepository.findById(savedComment.getId()).isEmpty(),
                "После удаления комментария нет");
    }
}
