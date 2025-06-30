package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Проверяет создание фильма с валидными данными
    @Test
    void shouldCreateFilmWithValidData() throws Exception {
        String validFilmJson = "{\"name\":\"Фильм\",\"description\":\"Описание\"," +
                "\"releaseDate\":\"2025-01-01\",\"duration\":120," +
                "\"mpa\":{\"id\":1}}";  // Исправленный JSON с запятой

        mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content(validFilmJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Фильм"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.mpa.id").value(1));
    }

    // Проверяет что при создании фильма с пустым названием возвращается статус 400 (Bad Request)
    @Test
    void shouldRejectFilmWithEmptyName() throws Exception {
        String invalidFilmJson = "{\"name\":\"\",\"description\":\"Описание\"," +
                "\"releaseDate\":\"2025-01-01\",\"duration\":120," +
                "\"mpa\":{\"id\":1}}";

        mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content(invalidFilmJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Название фильма не может быть пустым"));
    }

    // Проверяет валидацию продолжительности
    @Test
    void shouldRejectFilmWithNegativeDuration() throws Exception {
        String invalidFilmJson = "{\"name\":\"Фильм\",\"description\":\"Описание\"," +
                "\"releaseDate\":\"2025-01-01\",\"duration\":-1," +
                "\"mpa\":{\"id\":1}}";

        mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content(invalidFilmJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Продолжительность должна быть положительной"));
    }

    // Проверяет обновление несуществующего фильма
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentFilm() throws Exception {
        String filmJson = "{\"id\":999,\"name\":\"Фильм\"," +
                "\"description\":\"Описание\",\"releaseDate\":\"2025-01-01\"," +
                "\"duration\":120,\"mpa\":{\"id\":1}}";

        mockMvc.perform(put("/films")
                        .contentType("application/json")
                        .content(filmJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Фильм с id=999 не найден"));
    }

    // Проверяет получение всех фильмов
    @Test
    void shouldReturnAllFilms() throws Exception {
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    // Тест для популярных фильмов
    @Test
    void shouldReturnPopularFilms() throws Exception {
        mockMvc.perform(get("/films/popular?count=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // Тест для невалидного ID
    @Test
    void shouldRejectNegativeFilmId() throws Exception {
        mockMvc.perform(get("/films/-1"))
                .andExpect(status().isBadRequest());
    }

    // Тест для слишком длинного описания
    @Test
    void shouldRejectLongDescription() throws Exception {
        String longDesc = "A".repeat(201);
        String invalidFilmJson = "{ \"name\": \"Film\", \"description\": \"" + longDesc + "\", " +
                "\"releaseDate\": \"2025-01-01\", \"duration\": 120 }";

        mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content(invalidFilmJson))
                .andExpect(status().isBadRequest());
    }
}