package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    // Проверяет корректное создание и заполнение полей
    @Test
    void shouldCreateUserWithAllFields() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@mail.ru");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertEquals(1L, user.getId());
        assertEquals("test@mail.ru", user.getEmail());
        assertEquals("login", user.getLogin());
        assertEquals("Name", user.getName());
        assertEquals(LocalDate.of(1990, 1, 1), user.getBirthday());
    }

    // Проверяет работу сеттера имени
    @Test
    void shouldSetAndGetName() {
        User user = new User();
        user.setName("New Name");
        assertEquals("New Name", user.getName());
    }
}