package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.LikeDao;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikeDao likeDao;


    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage, LikeDao likeDao) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likeDao = likeDao;
    }

    public List<Film> getAllFilms() {
        List<Film> films = filmStorage.getAllFilms();
        log.debug("Текущее количество фильмов: {}", films.size());
        return films;
    }

    public Film getFilmById(long id) {
        log.debug("Получение фильма по ID: {}", id);
        return filmStorage.getFilmById(id);
    }

    public Film createFilm(Film film) {
        log.debug("Начало создания фильма: {}", film.getName());
        Film createdFilm = filmStorage.createFilm(film);
        log.info("Успешно создан фильм ID: {}, Название: {}", createdFilm.getId(), createdFilm.getName());
        return createdFilm;
    }

    public Film updateFilm(Film film) {
        log.debug("Запрос на обновление фильма с ID: {}", film.getId());
        Film updatedFilm = filmStorage.updateFilm(film);
        log.info("Фильм обновлён: ID={}, Название={}", updatedFilm.getId(), updatedFilm.getName());
        return updatedFilm;
    }

    public void addLike(long filmId, long userId) {
        log.debug("Обработка лайка. Фильм: {}, Пользователь: {}", filmId, userId);
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);
        likeDao.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(long filmId, long userId) {
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);
        likeDao.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            log.debug("Некорректное количество {}. Используем значение по умолчанию: 10", count);
            count = 10;
        }
        return filmStorage.getPopularFilms(count);
    }
}
