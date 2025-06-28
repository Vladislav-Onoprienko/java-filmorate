package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.DAO.GenreDao;
import ru.yandex.practicum.filmorate.validator.Constants;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class GenreService {
    private final GenreDao genreDao;

    public List<Genre> getAllGenres() {
        List<Genre> genres = genreDao.getAllGenres().stream()
                .filter(genre -> Constants.VALID_GENRE_IDS.contains(genre.getId()))
                .collect(Collectors.toList());
        log.debug("Текущее количество жанров: {}", genres.size());
        return genres;
    }

    public Genre getGenreById(int id) {
        log.debug("Получение жанра по ID: {}", id);
        Genre genre = genreDao.getGenreById(id);
        log.info("Найден жанр: ID={}, Название={}", genre.getId(), genre.getName());
        return genre;
    }
}