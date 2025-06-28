package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserServiceTest {

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private UserService userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser;
    private User secondUser;

    @BeforeEach
    void setUp() {
        // Очистка таблиц перед каждым тестом
        userStorage.clear();

        // Создание тестовых пользователей
        testUser = userStorage.createUser(
                User.builder()
                        .email("test@mail.ru")
                        .login("testLogin")
                        .name("Test User")
                        .birthday(LocalDate.of(1990, 1, 1))
                        .friends(new HashSet<>())
                        .build()
        );

        secondUser = userStorage.createUser(
                User.builder()
                        .email("second@mail.ru")
                        .login("secondLogin")
                        .name("Second User")
                        .birthday(LocalDate.of(1995, 5, 5))
                        .friends(new HashSet<>())
                        .build()
        );
    }

    // Проверяет успешное добавление друзей
    @Test
    void addFriend_ShouldAddFriendToBothUsers() {
        userService.addFriend(testUser.getId(), secondUser.getId());

        boolean friendshipExists = checkFriendshipInDatabase(testUser.getId(), secondUser.getId());
        assertTrue(friendshipExists, "Дружба должна быть добавлена в таблицу friendships");
    }

    // Проверяет исключение при попытке добавить себя в друзья
    @Test
    void addFriend_ShouldThrowWhenAddingSelf() {
        assertThrows(ValidationException.class,
                () -> userService.addFriend(testUser.getId(), testUser.getId()));
    }

    // Проверяет успешное удаление друга
    @Test
    void removeFriend_ShouldRemoveFriend() {
        userService.addFriend(testUser.getId(), secondUser.getId());
        userService.removeFriend(testUser.getId(), secondUser.getId());

        boolean friendshipExists = checkFriendshipInDatabase(testUser.getId(), secondUser.getId());
        assertFalse(friendshipExists, "Дружба должна быть удалена из таблицы friendships");
    }

    // Проверяет получение общих друзей
    @Test
    void getCommonFriends_ShouldReturnCommonFriends() {
        User commonFriend = userStorage.createUser(
                User.builder()
                        .email("friend@mail.ru")
                        .login("commonFriend")
                        .name("Common Friend")
                        .birthday(LocalDate.of(1992, 3, 3))
                        .build()
        );

        userService.addFriend(testUser.getId(), commonFriend.getId());
        userService.addFriend(secondUser.getId(), commonFriend.getId());

        List<User> commonFriends = userService.getCommonFriends(testUser.getId(), secondUser.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(commonFriend.getId(), commonFriends.get(0).getId());
    }

    // Проверяет исключение при попытке добавить несуществующего друга
    @Test
    void addFriend_ShouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> userService.addFriend(testUser.getId(), 999L));
    }

    // Проверяет создание пользователя
    @Test
    void createUser_ShouldCreateNewUser() {
        User newUser = User.builder()
                .email("new@mail.ru")
                .login("newuser")
                .name("New User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User createdUser = userService.createUser(newUser);

        assertNotNull(createdUser.getId());
        assertEquals("new@mail.ru", createdUser.getEmail());
    }

    // Проверяет обновление информации о пользователе
    @Test
    void updateUser_ShouldUpdateUserInfo() {
        testUser.setEmail("updated@mail.ru");
        User updatedUser = userService.updateUser(testUser);

        assertEquals("updated@mail.ru", updatedUser.getEmail());
    }

    // Проверяет исключение при попытке обновить несуществующего пользователя
    @Test
    void updateUser_ShouldThrowWhenUserNotFound() {
        User nonExistentUser = User.builder()
                .id(999L)
                .email("nonexistent@mail.ru")
                .login("nonexistent")
                .name("Non-existent User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        assertThrows(NotFoundException.class,
                () -> userService.updateUser(nonExistentUser));
    }

    // Проверяет получение пользователя по ID
    @Test
    void getUserById_ShouldReturnCorrectUser() {
        User result = userService.getUserById(testUser.getId());

        assertEquals(testUser.getId(), result.getId());
        assertEquals("test@mail.ru", result.getEmail());
    }

    // Проверяет исключение при запросе несуществующего пользователя
    @Test
    void getUserById_ShouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> userService.getUserById(999L));
    }

    // Проверяет получение всех пользователей
    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getId() == testUser.getId()));
        assertTrue(users.stream().anyMatch(u -> u.getId() == secondUser.getId()));
    }

    private boolean checkFriendshipInDatabase(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return count != null && count > 0;
    }
}