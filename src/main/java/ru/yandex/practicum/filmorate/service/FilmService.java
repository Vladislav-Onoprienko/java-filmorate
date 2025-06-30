package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmGenreRepository;
import ru.yandex.practicum.filmorate.storage.user.LikeRepository;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validator.FilmValidator;
import ru.yandex.practicum.filmorate.validator.GenreValidator;
import ru.yandex.practicum.filmorate.validator.MpaValidator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikeRepository likeRepository;
    private final FilmGenreRepository filmGenreRepository;
    private final MpaValidator mpaValidator;
    private final GenreValidator genreValidator;
    private final FilmValidator filmValidator;


    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       LikeRepository likeRepository,
                       FilmGenreRepository filmGenreRepository,
                       MpaValidator mpaValidator,
                       GenreValidator genreValidator,
                       FilmValidator filmValidator
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likeRepository = likeRepository;
        this.filmGenreRepository = filmGenreRepository;
        this.mpaValidator = mpaValidator;
        this.genreValidator = genreValidator;
        this.filmValidator = filmValidator;
    }

    public List<Film> getAllFilms() {
        List<Film> films = filmStorage.getAllFilms();
        loadGenresForFilms(films);
        log.debug("Текущее количество фильмов: {}", films.size());
        return films;
    }

    public Film getFilmById(long id) {
        log.debug("Получение фильма по ID: {}", id);
        Film film = filmStorage.getFilmById(id);
        film.setGenres(new LinkedHashSet<>(filmGenreRepository.getGenresForFilm(id)));
        return film;
    }

    public Film createFilm(Film film) {
        log.debug("Начало создания фильма: {}", film.getName());

        filmValidator.validateForCreate(film);
        mpaValidator.validateForCreate(film.getMpa().getId());
        genreValidator.validateForCreate(film.getGenres());

        Film createdFilm = filmStorage.createFilm(film);
        saveFilmGenres(createdFilm.getId(), film.getGenres());

        createdFilm.setGenres(film.getGenres() != null ?
                new LinkedHashSet<>(film.getGenres()) : new LinkedHashSet<>());
        log.info("Успешно создан фильм ID: {}, Название: {}", createdFilm.getId(), createdFilm.getName());
        return createdFilm;
    }

    public Film updateFilm(Film film) {
        log.debug("Запрос на обновление фильма с ID: {}", film.getId());

        filmValidator.validateForUpdate(film);
        mpaValidator.validateForCreate(film.getMpa().getId());
        genreValidator.validateForCreate(film.getGenres());

        Film updatedFilm = filmStorage.updateFilm(film);
        saveFilmGenres(film.getId(), film.getGenres());

        updatedFilm.setGenres(film.getGenres() != null ?
                new LinkedHashSet<>(film.getGenres()) : new LinkedHashSet<>());
        log.info("Фильм обновлён: ID={}, Название={}", updatedFilm.getId(), updatedFilm.getName());
        return updatedFilm;
    }

    public void addLike(long filmId, long userId) {
        log.debug("Обработка лайка. Фильм: {}, Пользователь: {}", filmId, userId);
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);
        likeRepository.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(long filmId, long userId) {
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);
        likeRepository.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            log.debug("Некорректное количество {}. Используем значение по умолчанию: 10", count);
            count = 10;
        }
        List<Film> films = filmStorage.getPopularFilms(count);
        loadGenresForFilms(films);
        return films;
    }

    private void loadGenresForFilms(List<Film> films) {
        if (films.isEmpty()) {
            log.debug("Запрос жанров: передан пустой список фильмов");
            return;
        }

        log.debug("Начало загрузки жанров для {} фильмов", films.size());

        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        Map<Long, List<Genre>> genresMap = filmGenreRepository.getGenresForFilms(filmIds);

        int filmsWithGenres = (int) films.stream()
                .filter(f -> genresMap.containsKey(f.getId()))
                .count();

        films.forEach(film ->
                film.setGenres(new LinkedHashSet<>(
                        genresMap.getOrDefault(film.getId(), List.of())
                ))
        );

        log.debug("Загружены жанры: {}/{} фильмов имеют жанры",
                filmsWithGenres, films.size());
    }

    private void saveFilmGenres(long filmId, Set<Genre> genres) {
        log.debug("Обновление жанров для фильма ID: {}", filmId);
        if (genres == null || genres.isEmpty()) {
            filmGenreRepository.removeGenresFromFilm(filmId);
        } else {
            filmGenreRepository.setGenresForFilm(filmId,
                    genres.stream()
                            .map(Genre::getId)
                            .collect(Collectors.toSet())
            );
        }
    }
}