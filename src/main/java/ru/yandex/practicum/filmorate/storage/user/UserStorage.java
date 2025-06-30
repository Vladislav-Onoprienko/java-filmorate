package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    List<User> getAllUsers();

    User getUserById(long id);

    List<User> getUsersByIds(List<Long> ids);

    User createUser(User user);

    User updateUser(User user);

    void clear();
}
