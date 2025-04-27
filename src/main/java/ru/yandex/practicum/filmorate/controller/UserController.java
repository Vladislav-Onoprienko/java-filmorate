package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 1;

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Валидация не пройдена: Email не может быть пустым");
            throw new ValidationException("Email не может быть пустым");
        }
        if (!user.getEmail().contains("@")) {
            log.warn("Валидация не пройдена: Email должен содержать @. Получено: {}", user.getEmail());
            throw new ValidationException("Email должен содержать @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Валидация не пройдена: Логин не может быть пустым");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.warn("Валидация не пройдена: Логин содержит пробелы: {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }

        if (user.getBirthday() == null) {
            log.warn("Валидация не пройдена: Дата рождения не указана");
            throw new ValidationException("Дата рождения обязательна");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Валидация не пройдена: Дата рождения в будущем: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.debug("Запрошен список пользователей. Текущее количество: {}", users.size());
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя не указано, используем логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }

        user.setId(idCounter++);
        users.put(user.getId(), user);
        log.info("Создан новый пользователь: ID={}, Email={}", user.getId(), user.getEmail());
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        if (!users.containsKey(user.getId())) {
            String errorMsg = "Пользователь с id=" + user.getId() + " не найден";
            log.error(errorMsg);
            throw new ValidationException(errorMsg);
        }
        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя не указано при обновлении, используем логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);
        log.info("Обновлен пользователь: ID={}", user.getId());
        return user;
    }
}
