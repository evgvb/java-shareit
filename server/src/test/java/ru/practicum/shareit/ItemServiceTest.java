package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@email.com")
                .build();

        booker = User.builder()
                .id(2L)
                .name("Booker")
                .email("booker@email.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();
    }

    @Test
    void createItem_WithValidRequestId_ShouldLinkToRequest() {
        ItemDto itemDto = ItemDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .requestId(1L)
                .build();

        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Request")
                .requestor(booker)
                .created(now.minusDays(1))
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(request));
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArgument(0));

        ItemDto result = itemService.createItem(itemDto, owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Item");
        verify(itemRequestRepository).findById(1L);
    }

    @Test
    void createItem_WithInvalidRequestId_ShouldThrowNoSuchElementException() {
        ItemDto itemDto = ItemDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .requestId(999L)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.createItem(itemDto, owner.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void updateItem_WithPartialUpdates_ShouldUpdateOnlyProvidedFields() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> updates = Map.of(
                "name", "New Name",
                "available", false
        );

        ItemDto result = itemService.updateItem(1L, updates, owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getDescription()).isEqualTo("Description"); // не изменилось
    }

    @Test
    void updateItem_WithEmptyUpdates_ShouldReturnOriginalItem() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArgument(0));

        ItemDto result = itemService.updateItem(1L, Map.of(), owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Item");
    }

    @Test
    void updateItem_WhenUserNotOwner_ShouldThrowNoSuchElementException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.updateItem(1L, Map.of("name", "New"), 999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("не найдена");
    }

    @Test
    void getItemById_WhenUserIsOwner_ShouldIncludeBookings() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        Booking pastBooking = Booking.builder()
                .id(1L)
                .start(now.minusDays(2))
                .end(now.minusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        Booking futureBooking = Booking.builder()
                .id(2L)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingRepository.findLastBookingForItem(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(pastBooking));
        when(bookingRepository.findNextBookingForItem(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(futureBooking));
        when(commentRepository.findAllByItemId(anyLong())).thenReturn(List.of());

        ItemDto result = itemService.getItemById(1L, owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getNextBooking()).isNotNull();
    }

    @Test
    void getAllItemsByOwner_WithPagination_ShouldReturnPaginatedResults() {
        when(userRepository.existsById(anyLong())).thenReturn(true);

        List<Item> items = List.of(
                Item.builder().id(1L).name("Item1").owner(owner).build(),
                Item.builder().id(2L).name("Item2").owner(owner).build(),
                Item.builder().id(3L).name("Item3").owner(owner).build()
        );

        when(itemRepository.findAllByOwnerId(anyLong())).thenReturn(items);
        when(bookingRepository.findAllApprovedByItemIds(anyList())).thenReturn(List.of());
        when(commentRepository.findAllByItemIdIn(anyList())).thenReturn(List.of());

        List<ItemDto> result = itemService.getAllItemsByOwner(owner.getId(), 1, 2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Item2");
        assertThat(result.get(1).getName()).isEqualTo("Item3");
    }

    @Test
    void searchItems_WithEmptyText_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.searchItems("", owner.getId(), 0, 10);

        assertThat(result).isEmpty();
        verify(itemRepository, never()).searchAvailableItems(anyString());
    }

    @Test
    void addComment_WhenUserNeverBooked_ShouldThrowValidationException() {
        CreateCommentDto commentDto = CreateCommentDto.builder()
                .text("Great item!")
                .build();

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.existsByItemIdAndBookerIdAndEndBefore(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> itemService.addComment(1L, commentDto, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("только после завершения");
    }

    @Test
    void addComment_WhenValid_ShouldSaveComment() {
        CreateCommentDto commentDto = CreateCommentDto.builder()
                .text("Great item!")
                .build();

        Comment savedComment = Comment.builder()
                .id(1L)
                .text("Great item!")
                .item(item)
                .author(booker)
                .created(now)
                .build();

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.existsByItemIdAndBookerIdAndEndBefore(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentDto result = itemService.addComment(1L, commentDto, booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Great item!");
        assertThat(result.getAuthorName()).isEqualTo("Booker");
    }

    @Test
    void deleteItem_WhenUserNotOwner_ShouldThrowNoSuchElementException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.deleteItem(1L, 999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("не найдена");
    }
}
