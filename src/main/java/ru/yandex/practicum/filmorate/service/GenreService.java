package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.general.GenreRepository;
import ru.yandex.practicum.filmorate.validator.Constants;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class GenreService {
    private final GenreRepository genreRepository;

    public List<Genre> getAllGenres() {
        List<Genre> genres = genreRepository.getAllGenres().stream()
                .filter(genre -> Constants.VALID_GENRE_IDS.contains(genre.getId()))
                .collect(Collectors.toList());
        log.debug("Текущее количество жанров: {}", genres.size());
        return genres;
    }

    public Genre getGenreById(int id) {
        log.debug("Получение жанра по ID: {}", id);
        Genre genre = genreRepository.getGenreById(id);
        log.info("Найден жанр: ID={}, Название={}", genre.getId(), genre.getName());
        return genre;
    }
}