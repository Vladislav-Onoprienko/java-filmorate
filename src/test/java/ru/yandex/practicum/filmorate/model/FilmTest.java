package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class FilmTest {

    // Проверяет корректное создание и заполнение полей
    @Test
    void shouldCreateFilmWithAllFields() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertEquals(1L, film.getId());
        assertEquals("Film", film.getName());
        assertEquals("Description", film.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), film.getReleaseDate());
        assertEquals(120, film.getDuration());
    }

    // Проверяет работу сеттера описания
    @Test
    void shouldSetAndGetDescription() {
        Film film = new Film();
        film.setDescription("New Description");
        assertEquals("New Description", film.getDescription());
    }
}
