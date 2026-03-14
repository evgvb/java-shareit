package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

    private Long id;

    @NotBlank(groups = {Create.class}, message = "Название не может быть пустым")
    private String name;

    @NotBlank(groups = {Create.class}, message = "Описание не может быть пустым")
    private String description;

    @NotNull(groups = {Create.class}, message = "Статус доступности должен быть")
    private Boolean available;

    private Long ownerId;

    private Long requestId;

    private BookingResponseDto lastBooking;
    private BookingResponseDto nextBooking;

    private List<CommentDto> comments;

    public interface Create {}

    public interface Update {}
}