package ru.yandex.practicum.filmorate.storage.DAO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
public class GenreDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> getAllGenres() {
        log.debug("Запрос всех жанров из БД");
        String sql = "SELECT * FROM genres ORDER BY genre_id";
        List<Genre> genres = jdbcTemplate.query(sql, this::mapRowToGenre);
        log.info("Получено {} жанров из БД", genres.size());
        return genres;
    }

    public Genre getGenreById(int id) {
        log.debug("Запрос жанра по ID: {}", id);
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        Genre genre = jdbcTemplate.query(sql, this::mapRowToGenre, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Жанр с ID {} не найден", id);
                    return new NotFoundException("Жанр не найден");
                });
        log.info("Найден жанр: ID={}, Name={}", genre.getId(), genre.getName());
        return genre;
    }

    public boolean existsById(long genreId) {
        log.debug("Проверка существования жанра с ID: {}", genreId);
        String sql = "SELECT EXISTS(SELECT 1 FROM genres WHERE genre_id = ?)";
        boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, genreId);
        log.debug("Результат проверки жанра ID {}: {}", genreId, exists);
        return exists;
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("genre_id"), rs.getString("name"));
    }
}
