package ru.yandex.practicum.filmorate.storage.DAO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class FriendshipDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FriendshipDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addFriendship(long userId, long friendId, String status) {
        log.debug("Добавление дружбы: пользователь {} → пользователь {}, статус: {}", userId, friendId, status);
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";
        int rows = jdbcTemplate.update(sql, userId, friendId, status);
        if (rows > 0) {
            log.info("Дружба добавлена: {} → {} (статус: {})", userId, friendId, status);
        }
    }

    public void updateFriendshipStatus(long userId, long friendId, String status) {
        log.debug("Обновление статуса дружбы: {} → {} на статус '{}'", userId, friendId, status);
        String sql = "UPDATE friendships SET status = ? WHERE user_id = ? AND friend_id = ?";
        int updated = jdbcTemplate.update(sql, status, userId, friendId);
        if (updated > 0) {
            log.info("Статус дружбы обновлен: {} → {} (новый статус: {})", userId, friendId, status);
        } else {
            log.warn("Дружба не найдена для обновления: {} → {}", userId, friendId);
        }
    }

    public void removeFriendship(long userId, long friendId) {
        log.debug("Удаление дружбы: {} → {}", userId, friendId);
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        int deleted = jdbcTemplate.update(sql, userId, friendId);
        if (deleted > 0) {
            log.info("Дружба удалена: {} → {}", userId, friendId);
        } else {
            log.warn("Дружба не найдена для удаления: {} → {}", userId, friendId);
        }
    }

    public List<Long> getFriends(long userId) {
        log.debug("Запрос подтвержденных друзей пользователя ID: {}", userId);
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        List<Long> friends = jdbcTemplate.queryForList(sql, Long.class, userId);
        log.trace("Найдено {} друзей для пользователя ID: {}", friends.size(), userId);
        return friends;
    }

    public boolean isFriendshipExists(long userId, long friendId) {
        log.debug("Проверка существования дружбы: {} → {}", userId, friendId);
        String sql = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        boolean exists = count > 0;
        log.trace("Результат проверки дружбы {} → {}: {}", userId, friendId, exists);
        return exists;
    }

    public void confirmFriendship(long userId, long friendId) {
        log.debug("Подтверждение дружбы: {} подтверждает заявку от {}", userId, friendId);
        String sql = "UPDATE friendships SET status = 'confirmed' WHERE user_id = ? AND friend_id = ?";
        int updated = jdbcTemplate.update(sql, friendId, userId);
        if (updated > 0) {
            log.info("Дружба подтверждена: {} ↔ {}", userId, friendId);
        } else {
            log.warn("Не удалось подтвердить дружбу: {} → {}", friendId, userId);
        }
    }
}