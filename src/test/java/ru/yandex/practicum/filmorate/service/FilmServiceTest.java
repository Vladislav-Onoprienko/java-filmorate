package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
class FilmServiceTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private FilmService filmService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Film testFilm;
    private User testUser;
    private User secondUser;

    @BeforeEach
    void setUp() {
        filmStorage.clear();
        userStorage.clear();

        testUser = userStorage.createUser(
                User.builder()
                        .email("test@mail.ru")
                        .login("testLogin")
                        .name("Test User")
                        .birthday(LocalDate.of(1990, 1, 1))
                        .build()
        );

        secondUser = userStorage.createUser(
                User.builder()
                        .email("second@mail.ru")
                        .login("secondLogin")
                        .name("Second User")
                        .birthday(LocalDate.of(1995, 5, 5))
                        .build()
        );

        testFilm = filmStorage.createFilm(
                Film.builder()
                        .name("Test Film")
                        .description("Test Description")
                        .releaseDate(LocalDate.of(2000, 1, 1))
                        .duration(120)
                        .mpa(new MpaRating(1, "G", "Нет возрастных ограничений"))
                        .likes(new HashSet<>())
                        .build()
        );
    }

    //Проверяет получение всех фильмов
    @Test
    void getAllFilms_ShouldReturnAllFilms() {
        Film secondFilm = filmStorage.createFilm(
                Film.builder()
                        .name("Second Film")
                        .description("Another Description")
                        .releaseDate(LocalDate.of(2005, 5, 5))
                        .duration(90)
                        .mpa(new MpaRating(2, "PG", "Рекомендуется присутствие родителей"))
                        .build()
        );

        List<Film> films = filmService.getAllFilms();
        assertEquals(2, films.size());
        assertTrue(films.stream().anyMatch(f -> f.getId() == testFilm.getId()));
        assertTrue(films.stream().anyMatch(f -> f.getId() == secondFilm.getId()));
    }

    //Проверяет получение фильма по ID
    @Test
    void getFilmById_ShouldReturnCorrectFilm() {
        Film result = filmService.getFilmById(testFilm.getId());

        assertEquals(testFilm.getId(), result.getId());
        assertEquals("Test Film", result.getName());
    }

    //Проверяет корректное обновление информации о фильме
    @Test
    void updateFilm_ShouldUpdateFilmInfo() {
        Film updatedFilm = Film.builder()
                .id(testFilm.getId())
                .name("Updated Title")
                .description("Updated Description")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(150)
                .mpa(new MpaRating(2, "PG", "Рекомендуется присутствие родителей"))
                .build();

        Film result = filmService.updateFilm(updatedFilm);

        assertEquals("Updated Title", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(150, result.getDuration());
    }

    //Проверяет успешное добавление лайка фильму
    @Test
    void addLike_ShouldAddLikeToFilm() {
        filmService.addLike(testFilm.getId(), testUser.getId());

        boolean likeExists = checkLikeInDatabase(testFilm.getId(), testUser.getId());
        assertTrue(likeExists, "Лайк должен быть добавлен в таблицу likes");

        Integer likesCount = getLikesCountFromDatabase(testFilm.getId());
        assertEquals(1, likesCount, "Количество лайков должно быть равно 1");
    }

    //Проверяет успешное удаление лайка
    @Test
    void removeLike_ShouldRemoveLike() {
        filmService.addLike(testFilm.getId(), testUser.getId());

        boolean likeExistsBefore = checkLikeInDatabase(testFilm.getId(), testUser.getId());
        assertTrue(likeExistsBefore, "Лайк должен быть добавлен в БД");

        filmService.removeLike(testFilm.getId(), testUser.getId());

        boolean likeExistsAfter = checkLikeInDatabase(testFilm.getId(), testUser.getId());
        assertFalse(likeExistsAfter, "Лайк должен быть удален из БД");
    }

    //Проверяет получение популярных фильмов с сортировкой по лайкам
    @Test
    void getPopularFilms_ShouldReturnFilmsSortedByLikes() {
        Film secondFilm = filmStorage.createFilm(
                Film.builder()
                        .name("Second Film")
                        .description("Another Description")
                        .releaseDate(LocalDate.of(2005, 5, 5))
                        .duration(90)
                        .mpa(new MpaRating(2, "PG", "Рекомендуется присутствие родителей"))
                        .build()
        );

        filmService.addLike(testFilm.getId(), testUser.getId());
        filmService.addLike(testFilm.getId(), secondUser.getId());
        filmService.addLike(secondFilm.getId(), testUser.getId());

        List<Film> popular = filmService.getPopularFilms(2);

        assertEquals(2, popular.size());
        assertEquals(testFilm.getId(), popular.get(0).getId()); // Больше лайков
        assertEquals(secondFilm.getId(), popular.get(1).getId());
    }

    //Проверяет исключение при попытке лайкнуть несуществующий фильм
    @Test
    void addLike_ShouldThrowWhenFilmNotFound() {
        assertThrows(NotFoundException.class,
                () -> filmService.addLike(999L, testUser.getId()));
    }

    //Проверяет исключение при попытке обновить несуществующий фильм
    @Test
    void updateFilm_ShouldThrowWhenFilmNotFound() {
        Film nonExistentFilm = Film.builder()
                .id(999L)
                .name("Non-existent Film")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpa(new MpaRating(1, "G", "Нет возрастных ограничений"))
                .build();

        assertThrows(NotFoundException.class,
                () -> filmService.updateFilm(nonExistentFilm));
    }

    //Проверяет исключение при запросе несуществующего фильма
    @Test
    void getFilmById_ShouldThrowWhenFilmNotFound() {
        assertThrows(NotFoundException.class,
                () -> filmService.getFilmById(999L));
    }

    private boolean checkLikeInDatabase(Long filmId, Long userId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId, userId);
        return count != null && count > 0;
    }

    private Integer getLikesCountFromDatabase(Long filmId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, filmId);
    }
}
