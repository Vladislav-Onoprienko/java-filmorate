package ru.yandex.practicum.filmorate.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    private UserController userController;

    @Autowired
    private MockMvc mockMvc;

    private Long testUserId;

    @AfterEach
    void tearDown() throws Exception {
        // Очищаем тестовые данные после каждого теста
        if (testUserId != null) {
            mockMvc.perform(delete("/users/" + testUserId));
        }
    }

    // Проверяет создание пользователя с валидными данными
    @Test
    void shouldCreateUserWithValidData() throws Exception {
        String uniqueEmail = "test" + System.currentTimeMillis() + "@mail.ru";
        String uniqueLogin = "validLogin" + System.currentTimeMillis();

        String validUserJson = String.format(
                "{ \"email\": \"%s\", \"login\": \"%s\", \"birthday\": \"2000-01-01\" }",
                uniqueEmail, uniqueLogin);

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(validUserJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(uniqueLogin))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        testUserId = JsonPath.parse(result.getResponse().getContentAsString())
                .read("$.id", Long.class);
    }

    // Проверяет замену пустого имени на логин
    @Test
    void shouldUseLoginAsNameWhenNameIsEmpty() throws Exception {
        String uniqueEmail = "test" + System.currentTimeMillis() + "@mail.ru";
        String uniqueLogin = "testLogin" + System.currentTimeMillis();

        String userJson = String.format(
                "{ \"email\": \"%s\", \"login\": \"%s\", \"name\": \"\", \"birthday\": \"2000-01-01\" }",
                uniqueEmail, uniqueLogin);

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(uniqueLogin))
                .andReturn();

        testUserId = JsonPath.parse(result.getResponse().getContentAsString())
                .read("$.id", Long.class);
    }

    // Проверяет валидацию email
    @Test
    void shouldRejectInvalidEmailFormat() throws Exception {
        String invalidUserJson = "{ \"email\": \"invalid-email\", \"login\": \"validLogin\", \"birthday\": \"2025-01-01\" }";

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(invalidUserJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Email должен быть валидным"));
    }

    // Проверяет валидацию логина с пробелами
    @Test
    void shouldRejectLoginWithSpaces() throws Exception {
        String invalidUserJson = "{ \"email\": \"test@mail.ru\", \"login\": \"invalid login\", \"birthday\": \"2025-01-01\" }";

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(invalidUserJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Логин не может содержать пробелы"));
    }

    // Проверяет обновление несуществующего пользователя
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentUser() throws Exception {
        String userJson = "{ \"id\": 999, \"email\": \"test@mail.ru\", " +
                "\"login\": \"validLogin\", \"birthday\": \"2025-01-01\" }";

        mockMvc.perform(put("/users")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isNotFound()) // Ожидаем статус 404
                .andExpect(jsonPath("$.message")
                        .value("Пользователь с id=999 не найден"));
    }
}