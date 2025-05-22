package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmServiceTest {

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private FilmStorage filmStorage;

    @Autowired
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        filmStorage.clear();
        userStorage.clear();
    }

    // Проверяет, что лайк от пользователя успешно добавляется к фильму
    @Test
    void addLike_ShouldAddLikeToFilm() {
        User user = new User(1L, "user1@mail.ru", "user1", "Name1", LocalDate.now(), new HashSet<>());
        userStorage.createUser(user);
        Film film = new Film(1L, "Фильм 1", "Описание фильма 1", LocalDate.now(), 120, new HashSet<>());
        filmStorage.createFilm(film);

        filmService.addLike(1L, 1L);

        assertTrue(filmStorage.getFilmById(1L).getLikes().contains(1L));
    }

    // Проверяет, что повторный лайк вызывает исключение ValidationException
    @Test
    void addLike_ShouldThrowWhenUserAlreadyLiked() {
        User user = new User(1L, "user1@mail.ru", "user1", "Name1", LocalDate.now(), new HashSet<>());
        userStorage.createUser(user);
        Film film = new Film(1L, "Фильм 1", "Описание фильма 1", LocalDate.now(), 120, new HashSet<>(Set.of(1L)));
        filmStorage.createFilm(film);

        assertThrows(ValidationException.class, () -> filmService.addLike(1L, 1L));
    }

    // Проверяет, что лайк пользователя можно успешно удалить из фильма
    @Test
    void removeLike_ShouldRemoveLike() {
        User user = new User(1L, "user1@mail.ru", "user1", "Name1", LocalDate.now(), new HashSet<>());
        userStorage.createUser(user);
        Film film = new Film(1L, "Фильм 1", "Описание фильма 1", LocalDate.now(), 120, new HashSet<>(Set.of(1L)));
        filmStorage.createFilm(film);

        filmService.removeLike(1L, 1L);

        assertFalse(filmStorage.getFilmById(1L).getLikes().contains(1L));
    }

    // Проверяет, что список популярных фильмов сортируется по количеству лайков по убыванию
    @Test
    void getPopularFilms_ShouldReturnFilmsSortedByLikes() {
        User user = new User(1L, "user1@mail.ru", "user1", "Name1", LocalDate.now(), new HashSet<>());
        userStorage.createUser(user);
        User user2 = new User(2L, "user2@mail.ru", "user2", "Name2", LocalDate.now(), new HashSet<>());
        userStorage.createUser(user2);

        Film film1 = new Film(1L, "Фильм 1", "Описание фильма 1", LocalDate.now(), 120, new HashSet<>(Set.of(1L, 2L)));
        filmStorage.createFilm(film1);
        Film film2 = new Film(2L, "Фильм 2", "Описание фильма 2", LocalDate.now(), 90, new HashSet<>(Set.of(1L)));
        filmStorage.createFilm(film2);

        List<Film> popular = filmService.getPopularFilms(2);

        assertEquals(1L, popular.get(0).getId());
        assertEquals(2L, popular.get(1).getId());
    }

    // Проверяет, что при попытке поставить лайк несуществующему фильму выбрасывается NotFoundException
    @Test
    void addLike_ShouldThrowWhenFilmNotFound() {
        User user = new User(1L, "user1@mail.ru", "user1", "Name1", LocalDate.now(), new HashSet<>());
        userStorage.createUser(user);

        assertThrows(NotFoundException.class, () -> filmService.addLike(999L, 1L));
    }

    //Проверяет, что при попытке удалить несуществующий лайк выбрасывается ValidationException
    @Test
    void removeLike_ShouldThrowWhenLikeNotExists() {
        User user = new User(1L, "user@mail.ru", "user", "Name", LocalDate.now(), new HashSet<>());
        userStorage.createUser(user);
        Film film = new Film(1L, "Фильм", "Описание", LocalDate.now(), 100, new HashSet<>());
        filmStorage.createFilm(film);

        assertThrows(NotFoundException.class, () -> filmService.removeLike(1L, 1L));
    }
}
