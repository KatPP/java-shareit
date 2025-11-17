package ru.practicum.shareit.user.mapper;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.User;

/**
 * Класс для преобразования между User и UserDto.
 */
public class UserMapper {
    public static UserDto toUserDto(User user) {
        if (user == null) return null;
        System.out.println("DEBUG: Mapping User to UserDto: " + user);
        UserDto dto = new UserDto(user.getId(), user.getName(), user.getEmail());
        System.out.println("DEBUG: Mapped UserDto: " + dto);
        return dto;
    }

    public static User toUser(UserDto userDto) {
        if (userDto == null) return null;
        System.out.println("DEBUG: Mapping UserDto to User: " + userDto);
        User user = new User();
        user.setId(userDto.getId());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        System.out.println("DEBUG: Mapped User: " + user);
        return user;
    }
}