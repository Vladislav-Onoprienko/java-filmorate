package ru.yandex.practicum.filmorate.storage.DAO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
}