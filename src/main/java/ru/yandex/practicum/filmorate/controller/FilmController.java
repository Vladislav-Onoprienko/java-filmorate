package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private long idCounter = 1;

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Валидация не пройдена: Название фильма пустое");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Валидация не пройдена: Описание слишком длинное ({} символов)",
                    film.getDescription().length());
            throw new ValidationException("Описание не может быть длиннее 200 символов");
        }

        if (film.getReleaseDate() == null) {
            log.warn("Валидация не пройдена: Дата релиза не указана");
            throw new ValidationException("Дата релиза обязательна");
        }
        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(minReleaseDate)) {
            log.warn("Валидация не пройдена: Дата релиза {} раньше допустимой ({})",
                    film.getReleaseDate(), minReleaseDate);
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() <= 0) {
            log.warn("Валидация не пройдена: Некорректная продолжительность: {}", film.getDuration());
            throw new ValidationException("Продолжительность должна быть положительной");
        }
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.debug("Запрошен список фильмов. Текущее количество: {}", films.size());
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        validateFilm(film);
        film.setId(idCounter++);
        films.put(film.getId(), film);
        log.info("Добавлен новый фильм: ID={}, Название={}", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        if (!films.containsKey(film.getId())) {
            String errorMsg = "Фильм с id=" + film.getId() + " не найден";
            log.error(errorMsg);
            throw new ValidationException(errorMsg);
        }
        validateFilm(film);
        films.put(film.getId(), film);
        log.info("Обновлен фильм: ID={}", film.getId());
        return film;
    }
}
