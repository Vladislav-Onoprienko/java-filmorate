package ru.yandex.practicum.filmorate.storage.general;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class GenreRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreRepository(JdbcTemplate jdbcTemplate) {
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

    public Set<Long> getExistingGenreIds(Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            log.debug("Запрос существующих жанров: передан пустой список ID");
            return Collections.emptySet();
        }

        log.debug("Запрос существующих жанров из списка ID: {}", genreIds);
        String inClause = genreIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String sql = String.format("SELECT genre_id FROM genres WHERE genre_id IN (%s)", inClause);

        Set<Long> existingIds = new HashSet<>(jdbcTemplate.queryForList(sql, Long.class));
        log.info("Найдено {} существующих жанров из {} запрошенных", existingIds.size(), genreIds.size());

        return existingIds;
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("genre_id"), rs.getString("name"));
    }
}
