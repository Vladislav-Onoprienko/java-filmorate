package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Repository("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long idCounter = 1;

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilmById(long id) {
        log.trace("Поиск фильма по ID: {}", id);
        if (!films.containsKey(id)) {
            log.error("Фильм с ID {} не найден", id);
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        log.debug("Успешно найден фильм ID: {}", id);
        return films.get(id);
    }

    @Override
    public Film createFilm(Film film) {
        log.info("Создание нового фильма. Название: {}", film.getName());
        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(minReleaseDate)) {
            log.error("Попытка создать фильм с недопустимой датой релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        film.setId(idCounter++);
        films.put(film.getId(), film);
        log.info("Добавлен новый фильм: ID={}, Название={}", film.getId(), film.getName());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        log.debug("Начало обновления фильма ID: {}", film.getId());
        if (!films.containsKey(film.getId())) {
            log.error("Попытка обновления несуществующего фильма ID: {}", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        log.info("Обновлен фильм: ID={}", film.getId());
        return film;
    }

    @Override
    public void clear() {
        films.clear();
        idCounter = 1;
        log.info("Хранилище фильмов очищено");
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        log.debug("Запрос {} популярных фильмов", count);

        if (count <= 0) {
            count = 10;
            log.debug("Установлено значение по умолчанию: 10");
        }

        List<Film> popularFilms = new ArrayList<>(films.values());
        popularFilms.sort((f1, f2) -> Integer.compare(
                f2.getLikes().size(),
                f1.getLikes().size()
        ));

        return popularFilms.stream()
                .limit(count)
                .collect(Collectors.toList());
    }
}