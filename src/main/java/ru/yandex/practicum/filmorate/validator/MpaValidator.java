package ru.yandex.practicum.filmorate.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;

import ru.yandex.practicum.filmorate.storage.film.MpaRepository;


@Component
public class MpaValidator {
    private final MpaRepository mpaRepository;

    @Autowired
    public MpaValidator(MpaRepository mpaRepository) {
        this.mpaRepository = mpaRepository;
    }

    public void validateForCreate(long mpaId) {
        validateId(mpaId);
        if (!mpaRepository.existsById(mpaId)) {
            throw new NotFoundException("MPA с id=" + mpaId + " не найден");
        }
    }

    private void validateId(long mpaId) {
        if (!Constants.VALID_MPA_IDS.contains(mpaId)) {
            throw new NotFoundException("Недопустимый MPA ID: " + mpaId);
        }
    }
}