package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

    // Проверяет создание фильма с валидными данными
    @Test
    void shouldCreateFilmWithValidData() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(120);

        Film createdFilm = filmController.addFilm(film);

        assertNotNull(createdFilm.getId(), "Должен создаваться фильм с ID");
        assertEquals("Фильм", createdFilm.getName(), "Название должно сохраняться");
    }

    // Проверяет валидацию пустого названия
    @Test
    void shouldRejectFilmWithEmptyName() {
        Film film = new Film();
        film.setName("");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class,
                () -> filmController.addFilm(film),
                "Должна быть ошибка при пустом названии");
    }

    // Проверяет валидацию продолжительности
    @Test
    void shouldRejectFilmWithNegativeDuration() {
        Film film = new Film();
        film.setName("Фильм");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(-1);

        assertThrows(ValidationException.class,
                () -> filmController.addFilm(film),
                "Должна быть ошибка при отрицательной продолжительности");
    }

    // Проверяет обновление несуществующего фильма
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentFilm() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Фильм");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class,
                () -> filmController.updateFilm(film),
                "Должна быть ошибка при обновлении несуществующего фильма");
    }
}