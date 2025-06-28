package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({FilmDbStorage.class, FilmGenreRepository.class})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM genres");
        jdbcTemplate.update("DELETE FROM mpa_ratings");

        jdbcTemplate.update("INSERT INTO mpa_ratings (mpa_id, name, description) VALUES (1, 'G', 'Нет возрастных ограничений')");
        jdbcTemplate.update("INSERT INTO genres (genre_id, name) VALUES (1, 'Комедия'), (2, 'Драма'), (3, 'Мультфильм')");

        jdbcTemplate.update("INSERT INTO films (film_id, name, description, release_date, duration, mpa_id) VALUES " +
                "(1, 'Film 1', 'Description 1', '2000-01-01', 120, 1), " +
                "(2, 'Film 2', 'Description 2', '2005-01-01', 90, 1)");

        jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (1, 1), (1, 2), (2, 3)");

        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (1, 1), (1, 2), (2, 1)");
    }

    // Тест проверяет корректность получения всех фильмов из базы данных
    @Test
    void testGetAllFilms() {
        List<Film> films = filmStorage.getAllFilms();

        // Проверяем что получено 2 фильма с правильными названиями
        assertThat(films)
                .hasSize(2)
                .extracting(Film::getName)
                .containsExactlyInAnyOrder("Film 1", "Film 2");

        // Проверяем что жанры корректно загружаются для каждого фильма
        assertThat(films.get(0).getGenres()).hasSize(2);
        assertThat(films.get(1).getGenres()).hasSize(1);
    }

    // Тест проверяет получение фильма по идентификатору
    @Test
    void testGetFilmById() {
        Film film = filmStorage.getFilmById(1);

        // Проверяем основные поля фильма
        assertThat(film)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Film 1")
                .hasFieldOrPropertyWithValue("description", "Description 1");

        // Проверяем что жанры загружены корректно
        assertThat(film.getGenres())
                .hasSize(2)
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder("Комедия", "Драма");
    }

    // Тест проверяет создание нового фильма
    @Test
    void testCreateFilm() {
        Film newFilm = Film.builder()
                .name("New Film")
                .description("New Description")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(100)
                .mpa(new MpaRating(1, "G", "Нет возрастных ограничений"))
                .genres(Set.of(new Genre(1, "Комедия")))
                .build();

        Film createdFilm = filmStorage.createFilm(newFilm);

        // Проверяем что фильм создан с правильными данными
        assertThat(createdFilm)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "New Film")
                .hasFieldOrPropertyWithValue("description", "New Description");

        // Проверяем что присвоен ID и жанры сохранены
        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getGenres()).hasSize(1);

        // Проверяем что фильм действительно сохранен в БД
        Film dbFilm = filmStorage.getFilmById(createdFilm.getId());
        assertThat(dbFilm).isEqualTo(createdFilm);
    }

    // Тест проверяет обновление существующего фильма
    @Test
    void testUpdateFilm() {
        Film film = filmStorage.getFilmById(1);

        film.setName("Updated Film");
        film.setDescription("Updated Description");
        film.setGenres(Set.of(new Genre(3, "Мультфильм")));

        Film updatedFilm = filmStorage.updateFilm(film);

        // Проверяем что данные обновились
        assertThat(updatedFilm)
                .hasFieldOrPropertyWithValue("name", "Updated Film")
                .hasFieldOrPropertyWithValue("description", "Updated Description");

        // Проверяем что жанры обновились
        assertThat(updatedFilm.getGenres())
                .hasSize(1)
                .extracting(Genre::getName)
                .containsExactly("Мультфильм");
    }

    // Тест проверяет получение популярных фильмов
    @Test
    void testGetPopularFilms() {
        List<Film> popularFilms = filmStorage.getPopularFilms(1);

        assertThat(popularFilms)
                .hasSize(1);
        assertThat(popularFilms.get(0).getName()).isEqualTo("Film 1");
        assertThat(popularFilms.get(0).getLikesCount()).isEqualTo(2);
    }
}
