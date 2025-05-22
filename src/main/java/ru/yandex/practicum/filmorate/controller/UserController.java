package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Запрошен список пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable long id) {
        log.info("Запрошен пользователь с ID: {}", id);
        User user = userService.getUserById(id);
        log.debug("Найден пользователь: ID={}, Email={}", user.getId(), user.getEmail());
        return user;
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable long id) {
        log.info("Запрос друзей пользователя с ID: {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        log.info("Запрос общих друзей пользователя {} c пользователем {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Запрос на создание пользователя с Email {}", user.getEmail());
        User createdUser = userService.createUser(user);
        log.info("Создан новый пользователь: ID={}, Email={}", createdUser.getId(), createdUser.getEmail());
        return createdUser;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Запрос на обновление пользователя c ID {}", user.getId());
        User updateUser = userService.updateUser(user);
        log.info("Обновлен пользователь: ID={}", user.getId());
        return updateUser;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("Запрос на добавление в друзья. Пользователь {} добавляет {}", id, friendId);
        userService.addFriend(id, friendId);
        log.info("Пользователи {} и {} теперь друзья", id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("Запрос от пользователя с ID {} на удаление из друзей пользователя с ID {}", id, friendId);
        userService.removeFriend(id, friendId);
        log.info("Дружба прекращена: пользователь {} и {}", id, friendId);
    }
}
