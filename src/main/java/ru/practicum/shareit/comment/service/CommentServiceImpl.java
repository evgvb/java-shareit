package ru.practicum.shareit.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    public CommentDto createComment(CommentDto commentDto, Long itemId, Long userId) {
        log.info("Создание комментария к вещи с ID: {} пользователем с ID: {}", itemId, userId);

        // Проверяем пользователя
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        // Проверяем вещь
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь не найдена"));

        // Проверяем текст комментария
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        // Проверяем, что пользователь брал вещь в аренду
        boolean hasBooking = bookingRepository.existsByItemIdAndBookerIdAndEndBefore(
                itemId, userId, LocalDateTime.now());

        if (!hasBooking) {
            throw new ValidationException("Только пользователи, которые брали вещь в аренду, могут оставлять комментарии");
        }

        // Создаем комментарий
        Comment comment = CommentMapper.toComment(commentDto, item, author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        log.info("Комментарий создан с ID: {}", savedComment.getId());
        return CommentMapper.toCommentDto(savedComment);
    }

    @Override
    public List<CommentDto> getCommentsByItemId(Long itemId) {
        log.info("Получение комментариев для вещи с ID: {}", itemId);

        // Проверяем существование вещи
        if (!itemRepository.existsById(itemId)) {
            throw new NoSuchElementException("Вещь не найдена");
        }

        return commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        log.info("Удаление комментария с ID: {} пользователем с ID: {}", commentId, userId);

        // Проверяем пользователя
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        // Находим комментарий
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Комментарий не найден"));

        // Проверяем, что пользователь - автор комментария или владелец вещи
        boolean isAuthor = comment.getAuthor().getId().equals(userId);
        boolean isOwner = comment.getItem().getOwner().getId().equals(userId);

        if (!isAuthor && !isOwner) {
            throw new ValidationException("Только автор комментария или владелец вещи могут удалить комментарий");
        }

        commentRepository.deleteById(commentId);
        log.info("Комментарий с ID {} удален", commentId);
    }
}