package ru.yandex.practicum.filmorate.validator;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@Component
public class FilmValidator {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    public void validateForCreate(Film film) {
        validateReleaseDate(film.getReleaseDate());
    }

    public void validateForUpdate(Film film) {
        validateReleaseDate(film.getReleaseDate());
    }

    private void validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate.isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}