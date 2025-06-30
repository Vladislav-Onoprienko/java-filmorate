-- Пользователи
INSERT INTO users (email, login, name, birthday) VALUES
('user1@example.com', 'user1', 'User One', '1990-01-01'),
('user2@example.com', 'user2', 'User Two', '1995-05-05');

-- Фильмы
INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES
('Film 1', 'Description 1', '2000-01-01', 120, 1),
('Film 2', 'Description 2', '2010-05-05', 90, 3);

-- Жанры для фильмов
INSERT INTO film_genres (film_id, genre_id) VALUES
(1, 1),
(1, 2),
(2, 4);

-- Лайки
INSERT INTO likes (film_id, user_id) VALUES
(1, 1),
(1, 2);