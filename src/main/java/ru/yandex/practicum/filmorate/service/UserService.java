package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        List<User> users = userStorage.getAllUsers();
        log.debug("Текущее количество пользователей: {}", users.size());
        return users;
    }

    public User getUserById(long id) {
        log.debug("Получение пользователя по ID: {}", id);
        return userStorage.getUserById(id);
    }

    public User createUser(User user) {
        log.debug("Начало создания пользователя: {}", user.getEmail());
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Для пользователя {} установлено имя из логина: {}", user.getEmail(),
                    user.getLogin());
            user.setName(user.getLogin());
        }
        User createdUser = userStorage.createUser(user);
        log.info("Успешно создан пользователь ID: {}, Email: {}", createdUser.getId(), createdUser.getEmail());
        return createdUser;
    }

    public User updateUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя не указано при обновлении, используем логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        return userStorage.updateUser(user);
    }

    public void addFriend(long userId, long friendId) {
        log.debug("Начало обработки добавления друга {} пользователю {}", friendId, userId);
        if (userId == friendId) {
            log.error("Попытка добавить самого себя в друзья: {}", userId);
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        if (user.getFriends().contains(friendId)) {
            log.warn("Попытка повторного добавления друга: пользователь {} уже имеет друга {}",
                    userId, friendId);
            throw new ValidationException("Пользователь уже в друзьях");
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
        log.info("Установлена дружба между пользователями: {} и {}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public List<User> getFriends(long userId) {
        User user = userStorage.getUserById(userId);
        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(long userId, long otherUserId) {
        log.debug("Поиск общих друзей для {} и {}", userId, otherUserId);

        List<User> commonFriends = userStorage.getUserById(userId).getFriends().stream()
                .filter(userStorage.getUserById(otherUserId).getFriends()::contains)
                .map(userStorage::getUserById)
                .collect(Collectors.toList());

        log.info("Найдено {} общих друзей между {} и {}",
                commonFriends.size(), userId, otherUserId);

        return commonFriends;
    }
}