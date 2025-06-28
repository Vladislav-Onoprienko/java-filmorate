package ru.yandex.practicum.filmorate.storage.DAO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(FriendshipDao.class)
class FriendshipDaoTest {

    @Autowired
    private FriendshipDao friendshipDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM users");

        jdbcTemplate.update("INSERT INTO users (user_id, email, login, name, birthday) VALUES " +
                "(1, 'user1@mail.ru', 'login1', 'User 1', '1990-01-01'), " +
                "(2, 'user2@mail.ru', 'login2', 'User 2', '1995-05-05')");
    }

    // Тест добавления дружбы
    @Test
    void testAddFriendship() {
        friendshipDao.addFriendship(1, 2, "confirmed");

        assertThat(friendshipDao.isFriendshipExists(1, 2)).isTrue();
    }

    // Тест обновления статуса дружбы
    @Test
    void testUpdateFriendshipStatus() {
        friendshipDao.addFriendship(1, 2, "pending");
        friendshipDao.updateFriendshipStatus(1, 2, "confirmed");

        assertThat(friendshipDao.getFriends(1)).containsExactly(2L);
    }

    // Тест удаления дружбы
    @Test
    void testRemoveFriendship() {
        friendshipDao.addFriendship(1, 2, "confirmed");
        friendshipDao.removeFriendship(1, 2);

        assertThat(friendshipDao.isFriendshipExists(1, 2)).isFalse();
    }

    // Тест получения списка друзей
    @Test
    void testGetFriends() {
        friendshipDao.addFriendship(1, 2, "confirmed");

        List<Long> friends = friendshipDao.getFriends(1);
        assertThat(friends).containsExactly(2L);
    }

    // Тест подтверждения дружбы
    @Test
    void testConfirmFriendship() {
        friendshipDao.addFriendship(1, 2, "unconfirmed");

        assertThat(friendshipDao.getFriends(1)).containsExactly(2L);

        friendshipDao.confirmFriendship(2, 1);

        assertThat(friendshipDao.getFriends(1)).containsExactly(2L);

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM friendships WHERE user_id = 1 AND friend_id = 2",
                String.class
        );
        assertThat(status).isEqualTo("confirmed");
    }
}
