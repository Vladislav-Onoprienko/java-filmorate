package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.FriendshipRepository;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipRepository friendshipRepository;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, FriendshipRepository friendshipRepository) {
        this.userStorage = userStorage;
        this.friendshipRepository = friendshipRepository;
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

        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);

        if (friendshipRepository.isFriendshipExists(userId, friendId)) {
            log.warn("Пользователь {} уже в друзьях у {}", friendId, userId);
            throw new ValidationException("Запрос на дружбу уже существует");
        }

        friendshipRepository.addFriendship(userId, friendId, "unconfirmed");
        log.info("Запрос на дружбу отправлен: от {} к {}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);

        friendshipRepository.removeFriendship(userId, friendId);
        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public List<User> getFriends(long userId) {
        log.debug("Запрос списка друзей пользователя {}", userId);
        userStorage.getUserById(userId);
        return friendshipRepository.getFriends(userId).stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(long userId, long otherUserId) {
        log.debug("Поиск общих друзей для {} и {}", userId, otherUserId);

        userStorage.getUserById(userId);
        userStorage.getUserById(otherUserId);

        List<Long> userFriends = friendshipRepository.getFriends(userId);
        List<Long> otherUserFriends = friendshipRepository.getFriends(otherUserId);

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public void confirmFriend(long userId, long friendId) {
        log.debug("Начало подтверждения дружбы: {} → {}", userId, friendId);

        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);

        if (!friendshipRepository.isFriendshipExists(friendId, userId)) {
            log.warn("Запрос на дружбу не найден: {} → {}", friendId, userId);
            throw new NotFoundException("Запрос на дружбу не найден");
        }

        friendshipRepository.confirmFriendship(userId, friendId);
        log.info("Дружба подтверждена: {} ↔ {}", userId, friendId);
    }
}