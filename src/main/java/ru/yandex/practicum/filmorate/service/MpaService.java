package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaDao;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaDao mpaDao;

    public List<MpaRating> getAllMpaRatings() {
        return mpaDao.getAllMpaRatings();
    }

    public MpaRating getMpaRatingById(int id) {
        return mpaDao.getMpaRatingById(id);
    }
}
