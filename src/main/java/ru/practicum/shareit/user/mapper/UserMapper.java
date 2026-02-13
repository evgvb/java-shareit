package ru.practicum.shareit.user.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Map;

@UtilityClass
public class UserMapper {

    public static UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User toUser(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public static User updateFromMap(User user, Map<String, Object> updates) {
        if (updates == null || updates.isEmpty()) {
            return user;
        }

        updates.forEach((key, value) -> {
            switch (key) {
                case "name":
                    if (value != null && !value.toString().isBlank()) {
                        user.setName(value.toString());
                    }
                    break;
                case "email":
                    if (value != null && !value.toString().isBlank()) {
                        user.setEmail(value.toString());
                    }
                    break;
            }
        });

        return user;
    }
}