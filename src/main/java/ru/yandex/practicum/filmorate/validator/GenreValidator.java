package ru.yandex.practicum.filmorate.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.general.GenreRepository;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GenreValidator {
    private final GenreRepository genreRepository;

    @Autowired
    public GenreValidator(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public void validateForCreate(Set<Genre> genres) {
        if (genres == null) return;

        genres.forEach(genre -> validateId(genre.getId()));

        Set<Long> genreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        Set<Long> existingIds = genreRepository.getExistingGenreIds(genreIds);

        Set<Long> notFoundIds = genreIds.stream()
                .filter(id -> !existingIds.contains(id))
                .collect(Collectors.toSet());

        if (!notFoundIds.isEmpty()) {
                throw new NotFoundException("Жанры с id=" + notFoundIds + " не найдены");
            }
    }

    private void validateId(long genreId) {
        if (!Constants.VALID_GENRE_IDS.contains(genreId)) {
            throw new NotFoundException("Недопустимый Genre ID: " + genreId);
        }
    }
}