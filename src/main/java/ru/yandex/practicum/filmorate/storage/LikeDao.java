package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class LikeDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public LikeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addLike(long filmId, long userId) {
        log.debug("Добавление лайка: пользователь {} → фильм {}", userId, filmId);
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        int rows = jdbcTemplate.update(sql, filmId, userId);
        if (rows > 0) {
            log.info("Лайк добавлен: пользователь {} → фильм {}", userId, filmId);
        }
    }

    public void removeLike(long filmId, long userId) {
        log.debug("Удаление лайка: пользователь {} → фильм {}", userId, filmId);
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        int deleted = jdbcTemplate.update(sql, filmId, userId);
        if (deleted > 0) {
            log.info("Лайк удален: пользователь {} → фильм {}", userId, filmId);
        } else {
            log.warn("Лайк не найден для удаления: пользователь {} → фильм {}", userId, filmId);
        }
    }

    public List<Long> getLikesByFilmId(long filmId) {
        log.debug("Запрос пользователей, лайкнувших фильм ID: {}", filmId);
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> likes = jdbcTemplate.queryForList(sql, Long.class, filmId);
        log.trace("Найдено {} лайков для фильма ID: {}", likes.size(), filmId);
        return likes;
    }

    public int getLikeCount(long filmId) {
        log.debug("Запрос количества лайков для фильма ID: {}", filmId);
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId);
        log.trace("Фильм ID: {} имеет {} лайков", filmId, count);
        return count != null ? count : 0;
    }
}