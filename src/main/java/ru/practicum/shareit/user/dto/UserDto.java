package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;

    @NotBlank(groups = {Create.class}, message = "Имя не может быть пустым")
    private String name;

    @NotBlank(groups = {Create.class}, message = "Email не может быть пустым")
    @Email(groups = {Create.class}, message = "Некорректный формат email")
    private String email;

    public interface Create {}

    public interface Update {}
}