package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    private UserController userController;

    @Autowired
    private MockMvc mockMvc;

    // Проверяет создание пользователя с валидными данными
    @Test
    void shouldCreateUserWithValidData() throws Exception {
        String validUserJson = "{ \"email\": \"test@mail.ru\", \"login\": \"validLogin\", \"birthday\": \"2025-01-01\" }";

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType("application/json")
                        .content(validUserJson))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.login").value("validLogin"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }

    // Проверяет замену пустого имени на логин
    @Test
    void shouldUseLoginAsNameWhenNameIsEmpty() throws Exception {
        String userJson = "{ \"email\": \"test@mail.ru\", \"login\": \"testLogin\", \"name\": \"\", \"birthday\": \"2025-01-01\" }";

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("testLogin"));
    }

    // Проверяет валидацию email
    @Test
    void shouldRejectInvalidEmailFormat() throws Exception {
        String invalidUserJson = "{ \"email\": \"invalid-email\", \"login\": \"validLogin\", \"birthday\": \"2025-01-01\" }";

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType("application/json")
                        .content(invalidUserJson))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email")
                        .value("Email должен быть валидным"));
    }

    // Проверяет валидацию логина с пробелами
    @Test
    void shouldRejectLoginWithSpaces() throws Exception {
        String invalidUserJson = "{ \"email\": \"test@mail.ru\", \"login\": \"invalid login\", \"birthday\": \"2025-01-01\" }";

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType("application/json")
                        .content(invalidUserJson))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.login")
                        .value("Логин не может содержать пробелы"));
    }

    // Проверяет обновление несуществующего пользователя
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentUser() throws Exception {
        String userJson = "{ \"id\": 999, \"email\": \"test@mail.ru\", " +
                "\"login\": \"validLogin\", \"birthday\": \"2025-01-01\" }";

        mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isNotFound()) // Ожидаем статус 404
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Пользователь с id=999 не найден"));
    }
}