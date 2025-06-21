package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmGenreDao filmGenreDao;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate,FilmGenreDao filmGenreDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmGenreDao = filmGenreDao;
    }

    @Override
    public List<Film> getAllFilms() {
        log.debug("Запрос всех фильмов из БД");
        String sql = "SELECT f.*, m.name AS mpa_name, m.description AS mpa_description " +
                "FROM films f JOIN mpa_ratings m ON f.mpa_id = m.mpa_id";
        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    @Override
    public Film getFilmById(long id) {
        log.debug("Поиск фильма по ID: {}", id);
        String sql = "SELECT f.*, m.name AS mpa_name, m.description AS mpa_description " +
                "FROM films f JOIN mpa_ratings m ON f.mpa_id = m.mpa_id WHERE f.film_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToFilm, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Фильм с id={} не найден", id);
                    return new NotFoundException("Фильм с id=" + id + " не найден");
                });
    }

    @Override
    public Film createFilm(Film film) {
        log.info("Создание нового фильма: {}", film.getName());
        if (isFilmExists(film.getName())) {
            throw new ValidationException("Фильм с таким названием уже существует");
        }

        // Сохраняем фильм
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setLong(4, film.getDuration());
            stmt.setLong(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        long filmId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        log.info("Фильм создан. ID: {}", filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            filmGenreDao.setGenresForFilm(filmId, genreIds);
        }

        return getFilmById(filmId);
    }

    @Override
    public Film updateFilm(Film film) {
        log.debug("Обновление фильма ID: {}", film.getId());

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE film_id = ?";

        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (updated == 0) {
            log.error("Фильм с id={} не найден для обновления", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        if (isFilmExists(film.getName(), film.getId())) {
            log.error("Попытка обновить фильм на существующее название: {}", film.getName());
            throw new ValidationException("Фильм с таким названием уже существует");
        }

        log.debug("Обновление жанров для фильма ID: {}", film.getId());
        filmGenreDao.removeGenresFromFilm(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            film.getGenres().forEach(genre ->
                    filmGenreDao.addGenreToFilm(film.getId(), genre.getId())
            );
        }

        log.info("Фильм ID: {} успешно обновлен", film.getId());
        return getFilmById(film.getId());
    }

    @Override
    public void clear() {
        log.info("Очистка всех данных о фильмах");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM films");
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        log.debug("Запрос {} популярных фильмов", count);
        if (count <= 0) {
            count = 10;
            log.debug("Установлено значение по умолчанию: 10");
        }

        String sql = "SELECT f.*, m.name AS mpa_name, m.description AS mpa_description, " +
                "COUNT(l.user_id) AS likes_count " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "GROUP BY f.film_id, m.name, m.description " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        List<Film> popularFilms = jdbcTemplate.query(sql, this::mapRowToFilm, count);
        log.info("Возвращено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = Film.builder()
                .id(rs.getLong("film_id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getLong("duration"))
                .mpa(new MpaRating(
                        rs.getLong("mpa_id"),
                        rs.getString("mpa_name"),
                        rs.getString("mpa_description")))
                .likesCount(rs.getInt("likes_count"))
                .build();

        film.setGenres(new HashSet<>(filmGenreDao.getGenresForFilm(film.getId())));

        return film;
    }

    private boolean isFilmExists(String name) {
        log.debug("Проверка существования фильма с названием '{}'", name);
        String sql = "SELECT COUNT(*) FROM films WHERE name = ?";
        boolean exists = jdbcTemplate.queryForObject(sql, Integer.class, name) > 0;
        log.trace("Фильм с названием '{}' существует: {}", name, exists);
        return exists;
    }

    private boolean isFilmExists(String name, long excludeId) {
        log.debug("Проверка существования фильма с названием '{}' (исключая ID {})", name, excludeId);
        String sql = "SELECT COUNT(*) FROM films WHERE name = ? AND film_id != ?";
        boolean exists = jdbcTemplate.queryForObject(sql, Integer.class, name, excludeId) > 0;
        log.trace("Фильм с названием '{}' (исключая ID {}) существует: {}", name, excludeId, exists);
        return exists;
    }
}
