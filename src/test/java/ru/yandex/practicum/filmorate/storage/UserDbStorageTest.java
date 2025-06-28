package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserDbStorage.class)
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM users");

        testUser = User.builder()
                .email("test@mail.ru")
                .login("testLogin")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    // Тест проверяет создание нового пользователя
    @Test
    void testCreateUser() {
        User createdUser = userStorage.createUser(testUser);

        // Проверяем что пользователь создан с правильными данными
        assertThat(createdUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("email", "test@mail.ru")
                .hasFieldOrPropertyWithValue("login", "testLogin");

        // Проверяем что присвоен ID
        assertThat(createdUser.getId()).isNotNull();

        // Проверяем что пользователь действительно сохранен в БД
        User dbUser = userStorage.getUserById(createdUser.getId());
        assertThat(dbUser).isEqualTo(createdUser);
    }

    // Тест проверяет исключение при создании пользователя с существующим email
    @Test
    void testCreateUserWithExistingEmailShouldThrow() {
        userStorage.createUser(testUser);

        User duplicateUser = User.builder()
                .email("test@mail.ru")
                .login("anotherLogin")
                .name("Duplicate User")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        // Проверяем что выбрасывается исключение
        assertThrows(ValidationException.class, () -> userStorage.createUser(duplicateUser));
    }

    // Тест проверяет обновление пользователя
    @Test
    void testUpdateUser() {
        User createdUser = userStorage.createUser(testUser);

        createdUser.setName("Updated Name");
        createdUser.setEmail("updated@mail.ru");

        User updatedUser = userStorage.updateUser(createdUser);

        // Проверяем что данные обновились
        assertThat(updatedUser)
                .hasFieldOrPropertyWithValue("name", "Updated Name")
                .hasFieldOrPropertyWithValue("email", "updated@mail.ru");

        // Проверяем что в БД сохранены обновленные данные
        User dbUser = userStorage.getUserById(createdUser.getId());
        assertThat(dbUser.getName()).isEqualTo("Updated Name");
    }

    // Тест проверяет исключение при обновлении несуществующего пользователя
    @Test
    void testUpdateNonExistingUserShouldThrow() {
        User nonExistingUser = User.builder()
                .id(999L)
                .email("nonexistent@mail.ru")
                .login("nonexistent")
                .name("Non-existent User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        assertThrows(NotFoundException.class, () -> userStorage.updateUser(nonExistingUser));
    }

    // Тест проверяет получение пользователя по ID
    @Test
    void testGetUserById() {
        User createdUser = userStorage.createUser(testUser);

        User foundUser = userStorage.getUserById(createdUser.getId());

        // Проверяем что получен правильный пользователь
        assertThat(foundUser)
                .isNotNull()
                .isEqualTo(createdUser);
    }

    // Тест проверяет исключение при запросе несуществующего пользователя
    @Test
    void testGetNonExistingUserByIdShouldThrow() {
        assertThrows(NotFoundException.class, () -> userStorage.getUserById(999L));
    }

    // Тест проверяет получение всех пользователей
    @Test
    void testGetAllUsers() {
        User secondUser = User.builder()
                .email("second@mail.ru")
                .login("secondLogin")
                .name("Second User")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        userStorage.createUser(testUser);
        userStorage.createUser(secondUser);

        List<User> users = userStorage.getAllUsers();

        // Проверяем что получено 2 пользователя
        assertThat(users)
                .hasSize(2)
                .extracting(User::getLogin)
                .containsExactlyInAnyOrder("testLogin", "secondLogin");
    }

    // Тест проверяет очистку хранилища
    @Test
    void testClear() {
        userStorage.createUser(testUser);

        userStorage.clear();

        // Проверяем что пользователей нет
        List<User> users = userStorage.getAllUsers();
        assertThat(users).isEmpty();
    }
}
