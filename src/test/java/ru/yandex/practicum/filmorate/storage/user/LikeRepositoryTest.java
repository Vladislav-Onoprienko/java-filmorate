package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(LikeRepository.class)
class LikeRepositoryTest {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM mpa_ratings");

        jdbcTemplate.update("INSERT INTO mpa_ratings (mpa_id, name, description) VALUES " +
                "(1, 'G', 'Нет ограничений')");

        jdbcTemplate.update("INSERT INTO films (film_id, name, description, release_date, duration, mpa_id) VALUES " +
                "(1, 'Фильм 1', 'Описание фильма', '2000-01-01', 120, 1)");

        jdbcTemplate.update("INSERT INTO users (user_id, email, login, name, birthday) VALUES " +
                "(1, 'user@mail.ru', 'login', 'Name', '1990-01-01')");
    }

    //Проверяет добавление лайка
    @Test
    void testAddLike() {
        likeRepository.addLike(1, 1);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = 1 AND user_id = 1",
                Integer.class);
        assertEquals(1, count);
    }

    //Проверяет удаление лайка
    @Test
    void testRemoveLike() {
        likeRepository.addLike(1, 1);
        likeRepository.removeLike(1, 1);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = 1 AND user_id = 1",
                Integer.class);
        assertEquals(0, count);
    }
}
