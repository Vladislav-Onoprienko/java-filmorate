package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
public class MpaRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MpaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MpaRating> getAllMpaRatings() {
        log.debug("Запрос всех рейтингов MPA из БД");
        String sql = "SELECT * FROM mpa_ratings ORDER BY mpa_id";
        List<MpaRating> ratings = jdbcTemplate.query(sql, this::mapRowToMpa);
        log.info("Получено {} рейтингов MPA из БД", ratings.size());
        return ratings;
    }

    public MpaRating getMpaRatingById(int id) {
        log.debug("Запрос рейтинга MPA по ID: {}", id);
        String sql = "SELECT * FROM mpa_ratings WHERE mpa_id = ?";
        MpaRating rating = jdbcTemplate.query(sql, this::mapRowToMpa, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Рейтинг MPA с ID {} не найден", id);
                    return new NotFoundException("Рейтинг MPA не найден");
                });
        log.info("Найден рейтинг MPA: ID={}, Name={}", rating.getId(), rating.getName());
        return rating;
    }

    public boolean existsById(long mpaId) {
        log.debug("Проверка существования рейтинга MPA с ID: {}", mpaId);
        String sql = "SELECT EXISTS(SELECT 1 FROM mpa_ratings WHERE mpa_id = ?)";
        boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, mpaId);
        log.debug("Результат проверки рейтинга MPA ID {}: {}", mpaId, exists);
        return exists;
    }

    private MpaRating mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        return new MpaRating(rs.getInt("mpa_id"), rs.getString("name"), rs.getString("description"));
    }
}