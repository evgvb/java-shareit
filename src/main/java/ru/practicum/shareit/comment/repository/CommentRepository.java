package ru.practicum.shareit.comment.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.comment.model.Comment;


import java.util.*;

@Repository
public class CommentRepository {

    private final Map<Long, Comment> comments = new HashMap<>();
    private final Map<Long, List<Comment>> itemComments = new HashMap<>();
    private Long idCounter = 1L;

    public Comment save(Comment comment) {
        if (comment.getId() == null) {
            comment.setId(idCounter++);
        }

        comments.put(comment.getId(), comment);

        Long itemId = comment.getItem().getId();
        itemComments.computeIfAbsent(itemId, k -> new ArrayList<>()).add(comment);

        return comment;
    }

    public Optional<Comment> findById(Long id) {
        return Optional.ofNullable(comments.get(id));
    }

    public List<Comment> findAllByItemId(Long itemId) {
        return itemComments.getOrDefault(itemId, Collections.emptyList());
    }

    public Map<Long, List<Comment>> findAllByItemIds(List<Long> itemIds) {
        Map<Long, List<Comment>> result = new HashMap<>();
        for (Long itemId : itemIds) {
            result.put(itemId, itemComments.getOrDefault(itemId, Collections.emptyList()));
        }
        return result;
    }

    public void deleteById(Long id) {
        Comment comment = comments.remove(id);
        if (comment != null) {
            Long itemId = comment.getItem().getId();
            List<Comment> itemCommentsList = itemComments.get(itemId);
            if (itemCommentsList != null) {
                itemCommentsList.remove(comment);
            }
        }
    }
}