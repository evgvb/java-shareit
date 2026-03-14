package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    @Test
    void toCommentDto_ShouldConvertCommentToDto() {
        User author = User.builder().id(1L).name("Author").build();
        Item item = Item.builder().id(10L).build();
        LocalDateTime now = LocalDateTime.now();

        Comment comment = Comment.builder()
                .id(100L)
                .text("Great item!")
                .item(item)
                .author(author)
                .created(now)
                .build();

        CommentDto dto = CommentMapper.toCommentDto(comment);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getText()).isEqualTo("Great item!");
        assertThat(dto.getAuthorName()).isEqualTo("Author");
        assertThat(dto.getCreated()).isEqualTo(now);
    }

    @Test
    void toCommentDto_ShouldReturnNull_WhenCommentIsNull() {
        assertThat(CommentMapper.toCommentDto(null)).isNull();
    }

    @Test
    void toComment_ShouldConvertCreateDtoToComment() {
        CreateCommentDto createDto = CreateCommentDto.builder()
                .text("Great item!")
                .build();

        Item item = Item.builder().id(10L).build();
        User author = User.builder().id(1L).name("Author").build();

        Comment comment = CommentMapper.toComment(createDto, item, author);

        assertThat(comment).isNotNull();
        assertThat(comment.getText()).isEqualTo("Great item!");
        assertThat(comment.getItem()).isEqualTo(item);
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getCreated()).isNotNull();
    }

    @Test
    void toComment_ShouldReturnNull_WhenCreateDtoIsNull() {
        assertThat(CommentMapper.toComment(null, null, null)).isNull();
    }
}