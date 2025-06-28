package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.DAO.MpaDao;
import ru.yandex.practicum.filmorate.validator.Constants;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class MpaService {
    private final MpaDao mpaDao;

    public List<MpaRating> getAllMpaRatings() {
        List<MpaRating> ratings = mpaDao.getAllMpaRatings().stream()
                .filter(mpa -> Constants.VALID_MPA_IDS.contains(mpa.getId()))
                .collect(Collectors.toList());
        log.debug("Текущее количество рейтингов MPA: {}", ratings.size());
        return ratings;
    }

    public MpaRating getMpaRatingById(int id) {
        log.debug("Получение рейтинга MPA по ID: {}", id);
        MpaRating rating = mpaDao.getMpaRatingById(id);
        log.info("Найден рейтинг MPA: ID={}, Название={}", rating.getId(), rating.getName());
        return rating;
    }
}