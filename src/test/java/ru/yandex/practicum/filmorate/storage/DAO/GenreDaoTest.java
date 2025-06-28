package ru.yandex.practicum.filmorate.storage.DAO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(GenreDao.class)
class GenreDaoTest {

    @Autowired
    private GenreDao genreDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM genres");

        jdbcTemplate.update("INSERT INTO genres (genre_id, name) VALUES " +
                "(1, 'Комедия'), " +
                "(2, 'Драма'), " +
                "(3, 'Мультфильм')");
    }

    // Проверяет получение всех жанров из базы данных
    @Test
    void testGetAllGenres() {
        List<Genre> genres = genreDao.getAllGenres();
        assertThat(genres)
                .hasSize(3)
                .extracting(Genre::getName)
                .containsExactly("Комедия", "Драма", "Мультфильм");
    }

    // Проверяет получение жанра по его ID
    @Test
    void testGetGenreById() {
        Genre genre = genreDao.getGenreById(1);
        assertThat(genre)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "Комедия");
    }

    // Проверяет существование жанра с указанным ID
    @Test
    void testExistsById() {
        assertThat(genreDao.existsById(1)).isTrue();
        assertThat(genreDao.existsById(999)).isFalse();
    }

    // Проверяет обработку случая, когда жанр с указанным ID не найден
    @Test
    void testGetGenreByIdNotFound() {
        assertThrows(NotFoundException.class, () -> genreDao.getGenreById(999));
    }
}