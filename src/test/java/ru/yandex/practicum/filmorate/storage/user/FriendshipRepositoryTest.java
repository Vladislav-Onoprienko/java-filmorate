package ru.yandex.practicum.filmorate.storage.user;

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
@Import(FriendshipRepository.class)
class FriendshipRepositoryTest {

    @Autowired
    private FriendshipRepository friendshipRepository;

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
        friendshipRepository.addFriendship(1, 2, "confirmed");

        assertThat(friendshipRepository.isFriendshipExists(1, 2)).isTrue();
    }

    // Тест обновления статуса дружбы
    @Test
    void testUpdateFriendshipStatus() {
        friendshipRepository.addFriendship(1, 2, "pending");
        friendshipRepository.updateFriendshipStatus(1, 2, "confirmed");

        assertThat(friendshipRepository.getFriends(1)).containsExactly(2L);
    }

    // Тест удаления дружбы
    @Test
    void testRemoveFriendship() {
        friendshipRepository.addFriendship(1, 2, "confirmed");
        friendshipRepository.removeFriendship(1, 2);

        assertThat(friendshipRepository.isFriendshipExists(1, 2)).isFalse();
    }

    // Тест получения списка друзей
    @Test
    void testGetFriends() {
        friendshipRepository.addFriendship(1, 2, "confirmed");

        List<Long> friends = friendshipRepository.getFriends(1);
        assertThat(friends).containsExactly(2L);
    }

    // Тест подтверждения дружбы
    @Test
    void testConfirmFriendship() {
        friendshipRepository.addFriendship(1, 2, "unconfirmed");

        assertThat(friendshipRepository.getFriends(1)).containsExactly(2L);

        friendshipRepository.confirmFriendship(2, 1);

        assertThat(friendshipRepository.getFriends(1)).containsExactly(2L);

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM friendships WHERE user_id = 1 AND friend_id = 2",
                String.class
        );
        assertThat(status).isEqualTo("confirmed");
    }
}
