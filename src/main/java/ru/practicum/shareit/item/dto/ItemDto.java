package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @NotNull(groups = {Create.class}, message = "Статус доступности не может быть null")
    private Boolean available;

    public interface Create {}

    public interface Update {}
}