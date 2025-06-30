package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 1;

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(long id) {
        log.trace("Поиск пользователя по ID: {}", id);
        if (!users.containsKey(id)) {
            log.error("Пользователь с ID {} не найден", id);
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        log.debug("Найден пользователь ID: {}", id);
        return users.get(id);
    }

    @Override
    public List<User> getUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.debug("Запрос пользователей: передан пустой список ID");
            return Collections.emptyList();
        }

        log.debug("Запрос пользователей по ID: {}", ids);
        List<User> result = ids.stream()
                .filter(users::containsKey)
                .map(users::get)
                .collect(Collectors.toList());

        log.info("Найдено {} пользователей из {} запрошенных", result.size(), ids.size());
        return result;
    }

    @Override
    public User createUser(User user) {
        user.setId(idCounter++);
        users.put(user.getId(), user);
        log.info("Создан новый пользователь: ID={}, Email={}", user.getId(), user.getEmail());
        return user;
    }

    @Override
    public User updateUser(User user) {
        log.debug("Начало обновления пользователя ID: {}", user.getId());
        if (!users.containsKey(user.getId())) {
            log.error("Попытка обновления несуществующего пользователя ID: {}", user.getId());
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        log.info("Обновлен пользователь: ID={}", user.getId());
        return user;
    }

    @Override
    public void clear() {
        users.clear();
        idCounter = 1;
        log.info("Хранилище пользователей очищено");
    }
}