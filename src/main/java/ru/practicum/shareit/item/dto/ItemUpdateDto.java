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
public class ItemUpdateDto {

    @NotBlank(groups = {ItemDto.Update.class}, message = "Название не может быть пустым, если указано")
    private String name;

    @NotBlank(groups = {ItemDto.Update.class}, message = "Описание не может быть пустым, если указано")
    private String description;

    @NotNull(groups = {ItemDto.Update.class}, message = "Статус доступности не может быть null")
    private Boolean available;
}