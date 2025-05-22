package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage.clear();
    }

    // Проверяет, что при добавлении друга — оба пользователя добавляются друг другу во множество друзей
    @Test
    void addFriend_ShouldAddFriendToBothUsers() {
        User user1 = new User(1L, "user1@mail.ru", "user1", "Name1", LocalDate.now(), new HashSet<>());
        User user2 = new User(2L, "user2@mail.ru", "user2", "Name2", LocalDate.now(), new HashSet<>());
        userStorage.createUser(user1);
        userStorage.createUser(user2);

        userService.addFriend(1L, 2L);

        assertTrue(userStorage.getUserById(1L).getFriends().contains(2L));
        assertTrue(userStorage.getUserById(2L).getFriends().contains(1L));
    }

    // Проверяет, что пользователь не может добавить сам себя в друзья (выбрасывается ValidationException)
    @Test
    void addFriend_ShouldThrowWhenAddingSelf() {
        User user = new User(1L, "user@mail.ru", "user", "Name", LocalDate.now(), new HashSet<>());
        userStorage.createUser(user);

        assertThrows(ValidationException.class, () -> userService.addFriend(1L, 1L));
    }

    // Проверяет, что при удалении друга — оба пользователя удаляют друг друга из списка друзей
    @Test
    void removeFriend_ShouldRemoveFriend() {
        User user1 = new User(1L, "user1@mail.ru", "user1", "Name1", LocalDate.now(), new HashSet<>(Set.of(2L)));
        User user2 = new User(2L, "user2@mail.ru", "user2", "Name2", LocalDate.now(), new HashSet<>(Set.of(1L)));
        userStorage.createUser(user1);
        userStorage.createUser(user2);

        userService.removeFriend(1L, 2L);

        assertFalse(userStorage.getUserById(1L).getFriends().contains(2L));
        assertFalse(userStorage.getUserById(2L).getFriends().contains(1L));
    }

    // Проверяет, что метод возвращает список общих друзей между двумя пользователями
    @Test
    void getCommonFriends_ShouldReturnCommonFriends() {
        User user1 = new User(1L, "user1@mail.ru", "user1", "Name1", LocalDate.now(), new HashSet<>(Set.of(3L)));
        User user2 = new User(2L, "user2@mail.ru", "user2", "Name2", LocalDate.now(), new HashSet<>(Set.of(3L)));
        User commonFriend = new User(3L, "friend@mail.ru", "friend", "Friend", LocalDate.now(), new HashSet<>());
        userStorage.createUser(user1);
        userStorage.createUser(user2);
        userStorage.createUser(commonFriend);

        List<User> commonFriends = userService.getCommonFriends(1L, 2L);

        assertEquals(1, commonFriends.size());
        assertEquals(3L, commonFriends.get(0).getId());
    }

    // Проверяет, что при попытке добавить в друзья несуществующего пользователя выбрасывается NotFoundException
    @Test
    void addFriend_ShouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class, () -> userService.addFriend(1L, 999L));
    }
}
