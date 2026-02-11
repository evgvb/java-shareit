package ru.practicum.shareit.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/items/{itemId}/comment")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(
            @PathVariable @Positive Long itemId,
            @Valid @RequestBody CommentDto commentDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("POST /items/{}/comment - создание комментария", itemId);
        return commentService.createComment(commentDto, itemId, userId);
    }

    @GetMapping
    public List<CommentDto> getCommentsByItemId(
            @PathVariable @Positive Long itemId) {
        log.info("GET /items/{}/comment - получение комментариев", itemId);
        return commentService.getCommentsByItemId(itemId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable @Positive Long itemId,
            @PathVariable @Positive Long commentId,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("DELETE /items/{}/comment/{} - удаление комментария", itemId, commentId);
        commentService.deleteComment(commentId, userId);
    }
}