package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    // Проверяет создание пользователя с валидными данными
    @Test
    void shouldCreateUserWithValidData() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(2025, 1, 1));

        User createdUser = userController.createUser(user);

        assertNotNull(createdUser.getId(), "Должен создаваться пользователь с ID");
        assertEquals("validLogin", createdUser.getLogin(), "Логин должен сохраняться");
    }

    // Проверяет замену пустого имени на логин
    @Test
    void shouldUseLoginAsNameWhenNameIsEmpty() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testLogin");
        user.setName("");
        user.setBirthday(LocalDate.of(2025, 1, 1));

        User createdUser = userController.createUser(user);

        assertEquals("testLogin", createdUser.getName(),
                "При пустом имени должен использоваться логин");
    }

    // Проверяет валидацию email
    @Test
    void shouldRejectInvalidEmailFormat() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(2025, 1, 1));

        assertThrows(ValidationException.class,
                () -> userController.createUser(user),
                "Должна быть ошибка при невалидном email");
    }

    // Проверяет валидацию логина с пробелами
    @Test
    void shouldRejectLoginWithSpaces() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("invalid login");
        user.setBirthday(LocalDate.of(2025, 1, 1));

        assertThrows(ValidationException.class,
                () -> userController.createUser(user),
                "Должна быть ошибка при логине с пробелами");
    }

    // Проверяет обновление несуществующего пользователя
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        User user = new User();
        user.setId(999L);
        user.setEmail("test@mail.ru");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(2025, 1, 1));

        assertThrows(ValidationException.class,
                () -> userController.updateUser(user),
                "Должна быть ошибка при обновлении несуществующего пользователя");
    }
}