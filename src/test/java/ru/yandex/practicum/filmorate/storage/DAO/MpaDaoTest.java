package ru.yandex.practicum.filmorate.storage.DAO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MpaDao.class)
class MpaDaoTest {

    @Autowired
    private MpaDao mpaDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM films");

        jdbcTemplate.update("DELETE FROM mpa_ratings");

        jdbcTemplate.update("INSERT INTO mpa_ratings (mpa_id, name, description) VALUES " +
                "(1, 'G', 'Нет возрастных ограничений'), " +
                "(2, 'PG', 'Рекомендуется с родителями'), " +
                "(3, 'PG-13', 'Детям до 13 лет просмотр нежелателен')");
    }

    // Проверяет получение всех рейтингов MPA
    @Test
    void testGetAllMpaRatings() {
        List<MpaRating> ratings = mpaDao.getAllMpaRatings();
        assertThat(ratings)
                .hasSize(3)
                .extracting(MpaRating::getName)
                .containsExactly("G", "PG", "PG-13");
    }

    // Проверяет получение рейтинга MPA по ID
    @Test
    void testGetMpaRatingById() {
        MpaRating rating = mpaDao.getMpaRatingById(1);
        assertThat(rating)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "G")
                .hasFieldOrPropertyWithValue("description", "Нет возрастных ограничений");
    }

    // Проверяет существование рейтинга MPA по ID
    @Test
    void testExistsById() {
        assertThat(mpaDao.existsById(1)).isTrue();
        assertThat(mpaDao.existsById(999)).isFalse();
    }

    // Проверяет обработку случая, когда рейтинг MPA не найден
    @Test
    void testGetMpaRatingByIdNotFound() {
        assertThrows(NotFoundException.class, () -> mpaDao.getMpaRatingById(999));
    }
}
