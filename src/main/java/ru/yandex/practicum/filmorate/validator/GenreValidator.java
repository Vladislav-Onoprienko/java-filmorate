package ru.yandex.practicum.filmorate.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.DAO.GenreDao;

import java.util.Set;

@Component
public class GenreValidator {
    private final GenreDao genreDao;

    @Autowired
    public GenreValidator(GenreDao genreDao) {
        this.genreDao = genreDao;
    }

    public void validateForCreate(Set<Genre> genres) {
        if (genres == null) return;

        genres.forEach(genre -> {
            validateId(genre.getId());
            if (!genreDao.existsById(genre.getId())) {
                throw new NotFoundException("Жанр с id=" + genre.getId() + " не найден");
            }
        });
    }

    private void validateId(long genreId) {
        if (!Constants.VALID_GENRE_IDS.contains(genreId)) {
            throw new NotFoundException("Недопустимый Genre ID: " + genreId);
        }
    }
}