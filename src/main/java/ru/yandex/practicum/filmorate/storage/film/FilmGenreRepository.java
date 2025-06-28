package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
public class FilmGenreRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmGenreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addGenreToFilm(long filmId, long genreId) {
        log.debug("Добавление жанра ID: {} к фильму ID: {}", genreId, filmId);
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, genreId);
    }

    public void removeGenresFromFilm(long filmId) {
        log.debug("Удаление всех жанров у фильма ID: {}", filmId);
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    public List<Genre> getGenresForFilm(long filmId) {
        log.debug("Запрос жанров для фильма ID: {}", filmId);
        String sql = "SELECT g.* FROM genres g JOIN film_genres fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id = ? " + "ORDER BY g.genre_id";
        return jdbcTemplate.query(sql, this::mapRowToGenre, filmId);
    }

    public void setGenresForFilm(long filmId, Set<Long> genreIds) {
        log.debug("Начало установки жанров для фильма ID: {}", filmId);

        int deletedRows = jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);
        log.debug("Удалено {} старых жанров для фильма ID: {}", deletedRows, filmId);

        List<Long> sortedGenreIds = genreIds.stream()
                .sorted()
                .toList();
        log.debug("Отсортированные ID жанров для добавления: {}", sortedGenreIds);

        if (!genreIds.isEmpty()) {
            int[] updateCounts = jdbcTemplate.batchUpdate(
                    "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                    sortedGenreIds.stream()
                            .map(genreId -> new Object[]{filmId, genreId})
                            .toList()
            );
            log.info("Добавлено {} жанров для фильма ID: {}", updateCounts.length, filmId);
        } else {
            log.info("Для фильма ID: {} не указано жанров (очищена связь)", filmId);
        }
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getLong("genre_id"), rs.getString("name"));
    }
}