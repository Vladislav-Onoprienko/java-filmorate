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
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Проверяет создание фильма с валидными данными
    @Test
    void shouldCreateFilmWithValidData() throws Exception {
        String validFilmJson = "{ \"name\": \"Фильм\", \"description\": \"Описание\", " +
                "\"releaseDate\": \"2025-01-01\", \"duration\": 120 }";

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType("application/json")
                        .content(validFilmJson))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Фильм"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }

    // Проверяет что при создании фильма с пустым названием возвращается статус 400 (Bad Request)
    @Test
    void shouldRejectFilmWithEmptyName() throws Exception {
        String invalidFilmJson = "{ \"name\": \"\", \"releaseDate\": \"2025-01-01\", \"duration\": 120 }";
        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType("application/json")
                        .content(invalidFilmJson))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400
                .andExpect(MockMvcResultMatchers.jsonPath("$.name")
                        .value("Название фильма не может быть пустым"));
    }

    // Проверяет валидацию продолжительности
    @Test
    void shouldRejectFilmWithNegativeDuration() throws Exception {
        String invalidFilmJson = "{ \"name\": \"Фильм\", \"releaseDate\": \"2025-01-01\", \"duration\": -1 }";

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType("application/json")
                        .content(invalidFilmJson))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.duration")
                        .value("Продолжительность должна быть положительной"));
    }

    // Проверяет обновление несуществующего фильма
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentFilm() throws Exception {
        String filmJson = "{ \"id\": 999, \"name\": \"Фильм\", " +
                "\"releaseDate\": \"2025-01-01\", \"duration\": 120 }";

        mockMvc.perform(MockMvcRequestBuilders.put("/films")
                        .contentType("application/json")
                        .content(filmJson))
                .andExpect(status().isNotFound()) // Ожидаем статус 404
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Фильм с id=999 не найден"));
    }
}