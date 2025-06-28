package ru.yandex.practicum.filmorate.storage.DAO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(FilmGenreDao.class)
class FilmGenreDaoTest {

    @Autowired
    private FilmGenreDao filmGenreDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM genres");
        jdbcTemplate.update("DELETE FROM mpa_ratings");

        jdbcTemplate.update("INSERT INTO mpa_ratings (mpa_id, name, description) VALUES " +
                "(1, 'G', 'Нет возрастных ограничений')");

        jdbcTemplate.update("INSERT INTO genres (genre_id, name) VALUES (1, 'Комедия'), (2, 'Драма')");

        jdbcTemplate.update("INSERT INTO films (film_id, name, description, release_date, duration, mpa_id) VALUES " +
                "(1, 'Фильм 1', 'Описание фильма', '2000-01-01', 120, 1)");
    }

    // Проверяет добавление жанра к фильму
    @Test
    void testAddGenreToFilm() {
        filmGenreDao.addGenreToFilm(1, 1);

        List<Genre> genres = filmGenreDao.getGenresForFilm(1);
        assertThat(genres)
                .hasSize(1)
                .extracting(Genre::getName)
                .containsExactly("Комедия");
    }

    // Проверяет удаление всех жанров у фильма
    @Test
    void testRemoveGenresFromFilm() {
        filmGenreDao.addGenreToFilm(1, 1);
        filmGenreDao.addGenreToFilm(1, 2);

        filmGenreDao.removeGenresFromFilm(1);

        assertThat(filmGenreDao.getGenresForFilm(1)).isEmpty();
    }

    // Проверяет получение списка жанров для фильма
    @Test
    void testGetGenresForFilm() {
        filmGenreDao.addGenreToFilm(1, 1);
        filmGenreDao.addGenreToFilm(1, 2);

        List<Genre> genres = filmGenreDao.getGenresForFilm(1);
        assertThat(genres)
                .hasSize(2)
                .extracting(Genre::getName)
                .containsExactly("Комедия", "Драма");
    }

    // Проверяет установку жанров для фильма (замену текущего списка жанров)
    @Test
    void testSetGenresForFilm() {
        filmGenreDao.setGenresForFilm(1, Set.of(1L, 2L));

        List<Genre> genres = filmGenreDao.getGenresForFilm(1);
        assertThat(genres)
                .hasSize(2)
                .extracting(Genre::getId)
                .containsExactly(1L, 2L);
    }
}
