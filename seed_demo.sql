-- =============================================================================
-- CultureLog - Script de datos de demo
-- =============================================================================
-- Contraseña de TODOS los usuarios: Demo1234!
-- (BCrypt hash generado con strength 10)
-- 
-- USO: Ejecutar contra la base de datos MySQL de CultureLog.
--      Este script BORRA todos los datos existentes antes de insertar los nuevos.
--
-- IMPORTANTE: Los externalId de cada item corresponden a IDs REALES de las APIs
--             proveedoras (TMDB, RAWG, JIKAN, DEEZER, OPEN_LIBRARY) para evitar
--             duplicados al buscar y añadir obras desde la aplicación.
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE notifications;
TRUNCATE TABLE post_saves;
TRUNCATE TABLE post_likes;
TRUNCATE TABLE comments;
TRUNCATE TABLE posts;
TRUNCATE TABLE media_tags;
TRUNCATE TABLE tags;
TRUNCATE TABLE media_items;
TRUNCATE TABLE follows;
TRUNCATE TABLE email_verification_tokens;
TRUNCATE TABLE password_reset_tokens;
TRUNCATE TABLE account_deletion_tokens;
TRUNCATE TABLE user_settings;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- USUARIOS (contraseña: Demo1234!)
-- =============================================================================
INSERT INTO users (id, username, password, email, profile_picture_url, banner_url, role, enabled, created_at) VALUES
(1, 'admin',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@culturelog.com',  'https://i.pravatar.cc/300?u=admin',  NULL, 'ADMIN', 1, '2025-09-01 10:00:00'),
(2, 'maria_gz',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'maria@example.com',     'https://i.pravatar.cc/300?u=maria',  NULL, 'USER',  1, '2025-10-15 14:30:00'),
(3, 'carlos_dev',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'carlos@example.com',    'https://i.pravatar.cc/300?u=carlos', NULL, 'USER',  1, '2025-11-02 09:15:00'),
(4, 'lucia_reads', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'lucia@example.com',     'https://i.pravatar.cc/300?u=lucia',  NULL, 'USER',  1, '2025-11-20 18:45:00'),
(5, 'pablo_cine',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'pablo@example.com',     'https://i.pravatar.cc/300?u=pablo',  NULL, 'USER',  1, '2025-12-05 11:00:00'),
(6, 'ana_music',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ana@example.com',       'https://i.pravatar.cc/300?u=ana',    NULL, 'USER',  1, '2026-01-10 16:20:00'),
(7, 'diego_gamer', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'diego@example.com',     'https://i.pravatar.cc/300?u=diego',  NULL, 'USER',  1, '2026-01-28 08:30:00'),
(8, 'elena_anime', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'elena@example.com',     'https://i.pravatar.cc/300?u=elena',  NULL, 'USER',  1, '2026-02-14 20:00:00');

-- =============================================================================
-- USER SETTINGS
-- =============================================================================
INSERT INTO user_settings (id, profile_privacy, show_future_list, allow_comments, theme, accent_color, email_notifications, user_id) VALUES
(1, 'PUBLICO',      1, 1, 'DARK',   '#448AFF', 1, 1),
(2, 'PUBLICO',      1, 1, 'DARK',   '#E040FB', 1, 2),
(3, 'PUBLICO',      1, 1, 'LIGHT',  '#69F0AE', 1, 3),
(4, 'PUBLICO',      0, 1, 'DARK',   '#FF4081', 1, 4),
(5, 'PUBLICO',      1, 1, 'DARK',   '#FFD740', 1, 5),
(6, 'PUBLICO',      1, 1, 'DARK',   '#18FFFF', 1, 6),
(7, 'SOLO_AMIGOS',  1, 1, 'DARK',   '#FF5252', 0, 7),
(8, 'PUBLICO',      1, 1, 'DARK',   '#E040FB', 1, 8);

-- =============================================================================
-- FOLLOWS
-- =============================================================================
INSERT INTO follows (id, follower_id, followed_id, status, created_at) VALUES
(1,  2, 1, 'ACCEPTED', '2025-10-16 10:00:00'),
(2,  3, 1, 'ACCEPTED', '2025-11-03 09:00:00'),
(3,  4, 1, 'ACCEPTED', '2025-11-21 12:00:00'),
(4,  5, 1, 'ACCEPTED', '2025-12-06 08:00:00'),
(5,  2, 3, 'ACCEPTED', '2025-11-05 14:00:00'),
(6,  3, 2, 'ACCEPTED', '2025-11-05 14:30:00'),
(7,  2, 4, 'ACCEPTED', '2025-11-22 10:00:00'),
(8,  4, 2, 'ACCEPTED', '2025-11-22 11:00:00'),
(9,  5, 3, 'ACCEPTED', '2025-12-10 16:00:00'),
(10, 6, 2, 'ACCEPTED', '2026-01-12 09:00:00'),
(11, 6, 5, 'ACCEPTED', '2026-01-12 09:05:00'),
(12, 7, 3, 'ACCEPTED', '2026-02-01 10:00:00'),
(13, 7, 5, 'ACCEPTED', '2026-02-01 10:05:00'),
(14, 8, 2, 'ACCEPTED', '2026-02-15 11:00:00'),
(15, 8, 4, 'ACCEPTED', '2026-02-15 11:05:00'),
(16, 8, 6, 'ACCEPTED', '2026-02-15 11:10:00'),
(17, 2, 5, 'ACCEPTED', '2026-01-20 15:00:00'),
(18, 3, 4, 'ACCEPTED', '2026-01-25 12:00:00'),
(19, 4, 5, 'ACCEPTED', '2026-02-01 08:00:00'),
(20, 5, 6, 'ACCEPTED', '2026-02-10 14:00:00'),
(21, 1, 2, 'ACCEPTED', '2025-10-16 10:30:00'),
(22, 1, 3, 'ACCEPTED', '2025-11-03 09:30:00'),
(23, 6, 8, 'ACCEPTED', '2026-03-01 10:00:00'),
(24, 8, 7, 'PENDING',  '2026-03-05 09:00:00');

-- =============================================================================
-- TAGS
-- =============================================================================
INSERT INTO tags (id, name, color, user_id) VALUES
(1,  'Favoritos',        'ROJO',      2),
(2,  'Clásicos',         'AMARILLO',  2),
(3,  'Recomendados',     'VERDE',     3),
(4,  'Para repetir',     'AZUL',      3),
(5,  'Obras maestras',   'MORADO',    4),
(6,  'Lectura verano',   'NARANJA',   4),
(7,  'Top 10',           'ROJO',      5),
(8,  'Cine europeo',     'CYAN',      5),
(9,  'Joyas ocultas',    'ROSA',      6),
(10, 'Banda sonora',     'MORADO',    6),
(11, 'Platinados',       'AMARILLO',  7),
(12, 'Pendiente DLC',    'NARANJA',   7),
(13, 'Shonen',           'ROJO',      8),
(14, 'Slice of Life',    'VERDE',     8);

-- =============================================================================
-- MEDIA ITEMS - maria_gz (user 2)
-- IDs verificados: TMDB 157336, 1396, 872585, 693134 | OPEN_LIBRARY /works/OL274505W
-- RAWG 51325 | DEEZER 568115892
-- =============================================================================
INSERT INTO media_items (id, title, type, status, genre, creator, rating, release_date, date_added, comment, description, item_imageurl, external_id, external_source, album, custom, user_id) VALUES
(1,  'Interstellar',          'PELICULA', 'VISTO', 'Ciencia ficción', 'Christopher Nolan', 5, '2014-11-07', '2026-01-15',
     'Una obra maestra visual y emocional. La escena de la ola gigante me dejó sin aliento.',
     'Un grupo de exploradores viaja a través de un agujero de gusano en el espacio en un intento de asegurar la supervivencia de la humanidad.',
     'https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg',
     '157336', 'TMDB', NULL, 0, 2),

(2,  'Breaking Bad',          'SERIE', 'VISTO', 'Drama, Crimen', 'Vince Gilligan', 5, '2008-01-20', '2026-01-20',
     'La mejor serie que he visto jamás. La evolución de Walter White es increíble.',
     'Un profesor de química de instituto se convierte en fabricante de metanfetamina tras ser diagnosticado con cáncer.',
     'https://image.tmdb.org/t/p/w500/ztkUQFLlC19CCMYHW73WxxWe8eZ.jpg',
     '1396', 'TMDB', NULL, 0, 2),

(3,  'Cien años de soledad',  'LIBRO', 'VISTO', 'Realismo mágico', 'Gabriel García Márquez', 5, '1967-06-05', '2026-02-01',
     'Magia pura en cada página. La historia de los Buendía es inolvidable.',
     'La historia de siete generaciones de la familia Buendía en el pueblo ficticio de Macondo.',
     'https://covers.openlibrary.org/b/id/8228691-L.jpg',
     '/works/OL274505W', 'OPEN_LIBRARY', NULL, 0, 2),

(4,  'The Last of Us Part II', 'VIDEOJUEGO', 'VISTO', 'Acción, Aventura', 'Naughty Dog', 4, '2020-06-19', '2026-02-10',
     'Historia brutal pero necesaria. El gameplay es espectacular.',
     'Cinco años después del peligroso viaje por Estados Unidos, Ellie y Joel se asientan en Jackson, Wyoming.',
     'https://media.rawg.io/media/games/909/909974d1c7863c2027241e265fe7011f.jpg',
     '51325', 'RAWG', NULL, 0, 2),

(5,  'Bohemian Rhapsody',     'MUSICA', 'VISTO', 'Rock', 'Queen', 5, '1975-10-31', '2026-02-15',
     'La mejor canción de la historia del rock. Punto.',
     'Sencillo de la banda británica Queen del álbum A Night at the Opera.',
     'https://e-cdns-images.dzcdn.net/images/cover/b2232c77a8af0f7b684e79be028b0737/500x500-000000-80-0-0.jpg',
     '568115892', 'DEEZER', 'A Night at the Opera', 0, 2),

(6,  'Oppenheimer',           'PELICULA', 'VISTO', 'Drama, Historia', 'Christopher Nolan', 5, '2023-07-21', '2026-03-01',
     'Nolan se supera a sí mismo. Cillian Murphy es brillante.',
     'La historia del físico J. Robert Oppenheimer y su papel en el desarrollo de la bomba atómica.',
     'https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg',
     '872585', 'TMDB', NULL, 0, 2),

(7,  'Dune: Parte Dos',       'PELICULA', 'POR_VER', 'Ciencia ficción', 'Denis Villeneuve', NULL, '2024-03-01', '2026-04-10',
     NULL,
     'Paul Atreides se une a los Fremen mientras busca venganza contra quienes destruyeron a su familia.',
     'https://image.tmdb.org/t/p/w500/8b8R8l88Qje9dn9OE8PY05Nez7H.jpg',
     '693134', 'TMDB', NULL, 0, 2);

-- =============================================================================
-- MEDIA ITEMS - carlos_dev (user 3)
-- IDs verificados: TMDB 603, 62560, 329865 | OPEN_LIBRARY /works/OL17798929W
-- RAWG 326243, 2551
-- =============================================================================
INSERT INTO media_items (id, title, type, status, genre, creator, rating, release_date, date_added, comment, description, item_imageurl, external_id, external_source, album, custom, user_id) VALUES
(8,  'The Matrix',            'PELICULA', 'VISTO', 'Ciencia ficción, Acción', 'Lana y Lilly Wachowski', 5, '1999-03-31', '2026-01-10',
     'Revolucionó el cine de acción para siempre. "There is no spoon."',
     'Un hacker descubre la verdadera naturaleza de la realidad y su papel en la guerra contra sus controladores.',
     'https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg',
     '603', 'TMDB', NULL, 0, 3),

(9,  'Clean Code',            'LIBRO', 'VISTO', 'Programación', 'Robert C. Martin', 4, '2008-08-01', '2026-01-18',
     'Lectura obligatoria para cualquier desarrollador. Algunos ejemplos algo anticuados pero los principios son eternos.',
     'Manual de artesanía del software que enseña las mejores prácticas de codificación limpia.',
     'https://covers.openlibrary.org/b/id/7222246-L.jpg',
     '/works/OL17798929W', 'OPEN_LIBRARY', NULL, 0, 3),

(10, 'Elden Ring',            'VIDEOJUEGO', 'VISTO', 'RPG, Acción', 'FromSoftware', 5, '2022-02-25', '2026-02-05',
     'Mejor RPG de la década. El mundo abierto es absurdamente bueno.',
     'Juego de rol de acción ambientado en un mundo de fantasía oscura creado por Hidetaka Miyazaki y George R.R. Martin.',
     'https://media.rawg.io/media/games/b29/b294fdd866dcdb643e7bab370a552855.jpg',
     '326243', 'RAWG', NULL, 0, 3),

(11, 'Mr. Robot',             'SERIE', 'VISTO', 'Drama, Thriller', 'Sam Esmail', 5, '2015-06-24', '2026-02-20',
     'La serie más realista sobre hacking y salud mental. El episodio sin cortes es cine.',
     'Un ingeniero de ciberseguridad y hacker es reclutado por un grupo anarquista para destruir las corporaciones que maneja.',
     'https://image.tmdb.org/t/p/w500/oKIBhzZzDX07SoE2bOLhq2EE8rf.jpg',
     '62560', 'TMDB', NULL, 0, 3),

(12, 'Dark Souls III',        'VIDEOJUEGO', 'VISTO', 'RPG, Acción', 'FromSoftware', 5, '2016-04-12', '2026-03-01',
     'La mejor despedida de la trilogía. Cada boss es memorable.',
     'El jugador asume el papel de un Cenizo, resucitado para devolver a los Señores de la Ceniza a sus tronos.',
     'https://media.rawg.io/media/games/da1/da1b267764d77221f07a4386b6548e5a.jpg',
     '2551', 'RAWG', NULL, 0, 3),

(13, 'Arrival',               'PELICULA', 'VISTO', 'Ciencia ficción, Drama', 'Denis Villeneuve', 4, '2016-11-11', '2026-03-10',
     'Ciencia ficción inteligente de verdad. La revelación final te deja pensando días.',
     'Una lingüista es reclutada por el ejército para comunicarse con extraterrestres que han llegado a la Tierra.',
     'https://image.tmdb.org/t/p/w500/x2FJsf1ElAgr63Y3PNPtJrcmpoe.jpg',
     '329865', 'TMDB', NULL, 0, 3);

-- =============================================================================
-- MEDIA ITEMS - lucia_reads (user 4)
-- IDs verificados: OPEN_LIBRARY /works/OL1168083W, /works/OL8074066W, /works/OL775065W,
--                  /works/OL17930368W, /works/OL20950884W | TMDB 93740
-- =============================================================================
INSERT INTO media_items (id, title, type, status, genre, creator, rating, release_date, date_added, comment, description, item_imageurl, external_id, external_source, album, custom, user_id) VALUES
(14, '1984',                  'LIBRO', 'VISTO', 'Distopía, Ficción política', 'George Orwell', 5, '1949-06-08', '2026-01-05',
     'Aterrador lo relevante que sigue siendo. Gran Hermano nos vigila.',
     'En un futuro distópico, un funcionario del Partido intenta rebelarse contra el control totalitario del Gran Hermano.',
     'https://covers.openlibrary.org/b/id/7222246-L.jpg',
     '/works/OL1168083W', 'OPEN_LIBRARY', NULL, 0, 4),

(15, 'El nombre del viento',  'LIBRO', 'VISTO', 'Fantasía épica', 'Patrick Rothfuss', 5, '2007-03-27', '2026-01-12',
     'La prosa más bonita que he leído en fantasía. Kvothe es fascinante.',
     'Kvothe, un aventurero y músico, narra su historia desde su infancia hasta convertirse en leyenda.',
     'https://covers.openlibrary.org/b/id/8269093-L.jpg',
     '/works/OL8074066W', 'OPEN_LIBRARY', NULL, 0, 4),

(16, 'Matar a un ruiseñor',   'LIBRO', 'VISTO', 'Ficción, Drama', 'Harper Lee', 5, '1960-07-11', '2026-02-08',
     'Atticus Finch es el mejor personaje de la literatura. Una lección de humanidad.',
     'A través de los ojos de Scout Finch, vemos a su padre defender a un hombre negro acusado injustamente en el sur de Estados Unidos.',
     'https://covers.openlibrary.org/b/id/8228691-L.jpg',
     '/works/OL775065W', 'OPEN_LIBRARY', NULL, 0, 4),

(17, 'Normal People',         'SERIE', 'VISTO', 'Drama, Romance', 'Lenny Abrahamson', 4, '2020-04-26', '2026-02-20',
     'Dolorosamente real. La química entre los protagonistas es mágica.',
     'Connell y Marianne crecen en la misma pequeña ciudad irlandesa pero sus vidas toman caminos muy diferentes.',
     'https://image.tmdb.org/t/p/w500/kkdUwMfVJHjl1bRMBGCusx9mHqi.jpg',
     '93740', 'TMDB', NULL, 0, 4),

(18, 'Sapiens',               'LIBRO', 'EN_PROGRESO', 'No ficción, Historia', 'Yuval Noah Harari', NULL, '2011-01-01', '2026-04-01',
     'Voy por el capítulo de la revolución agrícola. Muy interesante.',
     'Un recorrido por la historia de la humanidad desde la Edad de Piedra hasta el siglo XXI.',
     'https://covers.openlibrary.org/b/id/8406786-L.jpg',
     '/works/OL17930368W', 'OPEN_LIBRARY', NULL, 0, 4),

(19, 'Proyecto Hail Mary',    'LIBRO', 'POR_VER', 'Ciencia ficción', 'Andy Weir', NULL, '2021-05-04', '2026-04-15',
     NULL,
     'Un astronauta solitario debe salvar la Tierra de un desastre que amenaza con extinguir la vida.',
     'https://covers.openlibrary.org/b/id/10541864-L.jpg',
     '/works/OL20950884W', 'OPEN_LIBRARY', NULL, 0, 4);

-- =============================================================================
-- MEDIA ITEMS - pablo_cine (user 5)
-- IDs verificados: TMDB 496243, 550, 335984, 313369, 87108, 545611
-- =============================================================================
INSERT INTO media_items (id, title, type, status, genre, creator, rating, release_date, date_added, comment, description, item_imageurl, external_id, external_source, album, custom, user_id) VALUES
(20, 'Parásitos',             'PELICULA', 'VISTO', 'Thriller, Drama', 'Bong Joon-ho', 5, '2019-05-30', '2026-01-08',
     'Obra maestra absoluta. La mezcla de géneros es perfecta. Merece cada Oscar.',
     'La familia Kim, todos desempleados, se interesa por la adinerada familia Park y se infiltra en su hogar.',
     'https://image.tmdb.org/t/p/w500/7IiTTgloJzvGI1TAYymCfbfl3vT.jpg',
     '496243', 'TMDB', NULL, 0, 5),

(21, 'El club de la lucha',   'PELICULA', 'VISTO', 'Drama, Thriller', 'David Fincher', 5, '1999-10-15', '2026-01-20',
     'Primera regla: no hablar del club de la lucha. Una película que mejora con cada visionado.',
     'Un oficinista insomne y un fabricante de jabón forman un club de lucha clandestino.',
     'https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg',
     '550', 'TMDB', NULL, 0, 5),

(22, 'Blade Runner 2049',     'PELICULA', 'VISTO', 'Ciencia ficción, Drama', 'Denis Villeneuve', 5, '2017-10-06', '2026-02-14',
     'Visualmente la película más bella jamás filmada. Villeneuve es un genio.',
     'Un nuevo blade runner descubre un secreto que podría sumir a la sociedad en el caos.',
     'https://image.tmdb.org/t/p/w500/gajva2L0rPYkEWjzgFlBXCAVBE5.jpg',
     '335984', 'TMDB', NULL, 0, 5),

(23, 'La La Land',            'PELICULA', 'VISTO', 'Musical, Romance', 'Damien Chazelle', 4, '2016-12-09', '2026-03-01',
     'El final me destrozó. Las secuencias musicales son mágicas.',
     'Un pianista de jazz y una aspirante a actriz se enamoran en Los Ángeles.',
     'https://image.tmdb.org/t/p/w500/uDO8zWDhfWwoFdKS4fzkUJt0Rf0.jpg',
     '313369', 'TMDB', NULL, 0, 5),

(24, 'Chernobyl',             'SERIE', 'VISTO', 'Drama, Historia', 'Craig Mazin', 5, '2019-05-06', '2026-03-15',
     'La miniserie perfecta. Aterradora y fascinante a partes iguales.',
     'Dramatización del desastre nuclear de Chernóbil de 1986 y los sacrificios realizados para contenerlo.',
     'https://image.tmdb.org/t/p/w500/hlLXt2tOPT6RRnjiUmoxyG1LTFi.jpg',
     '87108', 'TMDB', NULL, 0, 5),

(25, 'Everything Everywhere All at Once', 'PELICULA', 'VISTO', 'Ciencia ficción, Comedia', 'Daniel Kwan, Daniel Scheinert', 4, '2022-03-25', '2026-04-05',
     'La creatividad más desbordante que he visto en cine. Risa y llanto en 2 horas.',
     'Una inmigrante china se ve envuelta en una aventura interdimensional mientras hace sus impuestos.',
     'https://image.tmdb.org/t/p/w500/w3LxiVYdWWRvEVdn5RYq6jIqkb1.jpg',
     '545611', 'TMDB', NULL, 0, 5);

-- =============================================================================
-- MEDIA ITEMS - ana_music (user 6)
-- IDs verificados: DEEZER 4562189 (Stairway rara, usamos track conocido),
--   DEEZER 6575789 (Paranoid Android/OK Computer track), DEEZER 116348768 (Come Together/Abbey Road)
--   TMDB 244786, 508442 | DEEZER 5765569 (Te Llevo Para Que Me Lleves)
-- =============================================================================
INSERT INTO media_items (id, title, type, status, genre, creator, rating, release_date, date_added, comment, description, item_imageurl, external_id, external_source, album, custom, user_id) VALUES
(26, 'Stairway to Heaven',    'MUSICA', 'VISTO', 'Rock', 'Led Zeppelin', 5, '1971-11-08', '2026-01-15',
     'La canción perfecta. Cada segundo construye sobre el anterior hasta la explosión final.',
     'Canción épica de Led Zeppelin del álbum Led Zeppelin IV.',
     'https://e-cdns-images.dzcdn.net/images/cover/9e5e2e76b3991a60f9e8e2a6cf65297d/500x500-000000-80-0-0.jpg',
     '4562189', 'DEEZER', 'Led Zeppelin IV', 0, 6),

(27, 'Paranoid Android',      'MUSICA', 'VISTO', 'Rock alternativo', 'Radiohead', 5, '1997-06-16', '2026-01-20',
     'El álbum OK Computer predijo el futuro. Paranoid Android es de otro planeta.',
     'Sencillo del tercer álbum de estudio de Radiohead, considerado uno de los mejores álbumes de la historia.',
     'https://e-cdns-images.dzcdn.net/images/cover/faa4b5e4e42b4e9a9345e0ab79a83c22/500x500-000000-80-0-0.jpg',
     '6575789', 'DEEZER', 'OK Computer', 0, 6),

(28, 'Come Together',         'MUSICA', 'VISTO', 'Rock', 'The Beatles', 5, '1969-09-26', '2026-02-10',
     'La despedida perfecta de los Beatles. El medley del lado B es lo mejor que han hecho.',
     'Canción de apertura de Abbey Road, undécimo álbum de estudio de The Beatles.',
     'https://e-cdns-images.dzcdn.net/images/cover/80a7e84be24f33db2e3e9b89b0a27d39/500x500-000000-80-0-0.jpg',
     '116348768', 'DEEZER', 'Abbey Road', 0, 6),

(29, 'Whiplash',              'PELICULA', 'VISTO', 'Drama, Música', 'Damien Chazelle', 5, '2014-10-10', '2026-02-20',
     'La película que hizo que quisiera tocar batería. J.K. Simmons da miedo.',
     'Un joven baterista se inscribe en un conservatorio donde un temible instructor le empuja al límite.',
     'https://image.tmdb.org/t/p/w500/7fn624j5lj3xTme2SgiLCeuedmO.jpg',
     '244786', 'TMDB', NULL, 0, 6),

(30, 'Te Llevo Para Que Me Lleves', 'MUSICA', 'VISTO', 'Rock', 'Gustavo Cerati', 5, '1993-10-04', '2026-03-05',
     'Cerati en su momento más íntimo y experimental. Pura magia.',
     'Canción del primer álbum solista de Gustavo Cerati, Amor Amarillo.',
     'https://e-cdns-images.dzcdn.net/images/cover/3e0cb7a33efcb12f51dce5bdd4bdc5cc/500x500-000000-80-0-0.jpg',
     '5765569', 'DEEZER', 'Amor Amarillo', 0, 6),

(31, 'Soul',                  'PELICULA', 'VISTO', 'Animación, Drama', 'Pete Docter', 4, '2020-12-25', '2026-03-20',
     'Pixar hablando del sentido de la vida con jazz de fondo. Precioso.',
     'Un profesor de música de secundaria tiene la oportunidad de tocar en el mejor club de jazz de Nueva York.',
     'https://image.tmdb.org/t/p/w500/hm58Jw4Lw8OIeECIq5qyPYhAeRJ.jpg',
     '508442', 'TMDB', NULL, 0, 6);

-- =============================================================================
-- MEDIA ITEMS - diego_gamer (user 7)
-- IDs verificados: RAWG 3328, 28, 9767, 22121, 274755, 324997
-- (Silksong no tiene ID real en RAWG aún, se marca custom)
-- =============================================================================
INSERT INTO media_items (id, title, type, status, genre, creator, rating, release_date, date_added, comment, description, item_imageurl, external_id, external_source, album, custom, user_id) VALUES
(32, 'The Witcher 3: Wild Hunt', 'VIDEOJUEGO', 'VISTO', 'RPG, Acción', 'CD Projekt RED', 5, '2015-05-19', '2026-02-01',
     'El mejor RPG jamás creado. 200 horas y quiero más. Las misiones secundarias superan a muchos juegos principales.',
     'Geralt de Rivia busca a su hija adoptiva mientras una fuerza sobrenatural amenaza el mundo.',
     'https://media.rawg.io/media/games/618/618c2031a07bbff6b4f611f10b6bcdbc.jpg',
     '3328', 'RAWG', NULL, 0, 7),

(33, 'Red Dead Redemption 2',    'VIDEOJUEGO', 'VISTO', 'Acción, Aventura', 'Rockstar Games', 5, '2018-10-26', '2026-02-10',
     'Arthur Morgan es el mejor protagonista de la historia de los videojuegos. Sin discusión.',
     'La épica historia de Arthur Morgan y la banda de Van der Linde en el ocaso del salvaje oeste americano.',
     'https://media.rawg.io/media/games/511/5118aff5091cb3efec399c808f8c598f.jpg',
     '28', 'RAWG', NULL, 0, 7),

(34, 'Hollow Knight',            'VIDEOJUEGO', 'VISTO', 'Metroidvania, Acción', 'Team Cherry', 5, '2017-02-24', '2026-02-20',
     'Un indie que compite con AAA. El Panteón de Hallownest casi me hace romper el mando.',
     'Un caballero insectoide explora las profundidades de un reino en ruinas lleno de criaturas y secretos.',
     'https://media.rawg.io/media/games/4cf/4cfc6b7f1850590a4634b08bfab308ab.jpg',
     '9767', 'RAWG', NULL, 0, 7),

(35, 'Celeste',                  'VIDEOJUEGO', 'VISTO', 'Plataformas', 'Maddy Thorson', 5, '2018-01-25', '2026-03-05',
     'Un plataformas sobre ansiedad y superación personal. Cada muerte enseña algo.',
     'Madeline decide escalar la montaña Celeste enfrentándose a sus demonios internos.',
     'https://media.rawg.io/media/games/594/59487800889ebac294c7c2c070d02356.jpg',
     '22121', 'RAWG', NULL, 0, 7),

(36, 'Hades',                    'VIDEOJUEGO', 'VISTO', 'Roguelike, Acción', 'Supergiant Games', 5, '2020-09-17', '2026-03-15',
     'El mejor roguelike de la historia. La narrativa integrada en el gameplay es brillante.',
     'Zagreus, hijo de Hades, intenta escapar del inframundo desafiando a dioses y monstruos griegos.',
     'https://media.rawg.io/media/games/1f4/1f47a270b8f241e4676b14d39ec620f7.jpg',
     '274755', 'RAWG', NULL, 0, 7),

(37, 'Baldur''s Gate 3',         'VIDEOJUEGO', 'EN_PROGRESO', 'RPG, Estrategia', 'Larian Studios', NULL, '2023-08-03', '2026-04-01',
     'Llevo 60 horas y sigo en el Acto 2. Cada decisión importa de verdad.',
     'Un grupo de aventureros infectados con parásitos ílidos deben encontrar una cura mientras enfrentan fuerzas oscuras.',
     'https://media.rawg.io/media/games/699/69907ecf13f172e9e144069769c3be73.jpg',
     '324997', 'RAWG', NULL, 0, 7);

-- =============================================================================
-- MEDIA ITEMS - elena_anime (user 8)
-- IDs verificados: JIKAN (MAL) anime 5114, 16498, 50265 | manga 116778, 13
-- TMDB 372058
-- =============================================================================
INSERT INTO media_items (id, title, type, status, genre, creator, rating, release_date, date_added, comment, description, item_imageurl, external_id, external_source, album, custom, user_id) VALUES
(38, 'Fullmetal Alchemist: Brotherhood', 'ANIME', 'VISTO', 'Acción, Aventura, Fantasía', 'Hiromu Arakawa', 5, '2009-04-05', '2026-02-16',
     'El anime perfecto. Historia, personajes, acción, emoción: todo en su punto.',
     'Dos hermanos alquimistas buscan la piedra filosofal para restaurar sus cuerpos tras un experimento fallido.',
     'https://cdn.myanimelist.net/images/anime/1208/94745l.jpg',
     '5114', 'JIKAN', NULL, 0, 8),

(39, 'Attack on Titan',          'ANIME', 'VISTO', 'Acción, Drama, Fantasía', 'Hajime Isayama', 5, '2013-04-07', '2026-02-20',
     'El plot twist del sótano cambió todo. La evolución de Eren es brutal.',
     'La humanidad vive encerrada tras enormes murallas para protegerse de los Titanes, gigantes devoradores de humanos.',
     'https://cdn.myanimelist.net/images/anime/10/47347l.jpg',
     '16498', 'JIKAN', NULL, 0, 8),

(40, 'Chainsaw Man',             'MANGA', 'VISTO', 'Acción, Sobrenatural', 'Tatsuki Fujimoto', 5, '2018-12-03', '2026-03-01',
     'Fujimoto es un genio loco. El ritmo narrativo es único en el manga actual.',
     'Denji, un joven cazador de demonios, se fusiona con su demonio motosierra mascota para sobrevivir.',
     'https://cdn.myanimelist.net/images/manga/3/216464l.jpg',
     '116778', 'JIKAN', NULL, 0, 8),

(41, 'Your Name',                'PELICULA', 'VISTO', 'Anime, Romance, Fantasía', 'Makoto Shinkai', 5, '2016-08-26', '2026-03-10',
     'Lloré como nunca. La animación es de otro mundo. Shinkai superó a Ghibli.',
     'Dos adolescentes descubren que están intercambiando cuerpos misteriosamente y deciden buscarse.',
     'https://image.tmdb.org/t/p/w500/q719jXXEzOoYaps6babgKnONONX.jpg',
     '372058', 'TMDB', NULL, 0, 8),

(42, 'Spy x Family',             'ANIME', 'EN_PROGRESO', 'Comedia, Acción', 'Tatsuya Endo', NULL, '2022-04-09', '2026-04-01',
     'Anya es el mejor personaje del anime moderno. Waku waku!',
     'Un espía, una asesina y una telépata forman una familia falsa sin conocer los secretos de los demás.',
     'https://cdn.myanimelist.net/images/anime/1441/122795l.jpg',
     '50265', 'JIKAN', NULL, 0, 8),

(43, 'One Piece',                'MANGA', 'EN_PROGRESO', 'Aventura, Acción', 'Eiichiro Oda', NULL, '1997-07-22', '2026-02-18',
     'Llevo 1100 capítulos y cada arco me sorprende más. La saga final promete.',
     'Monkey D. Luffy y su tripulación navegan los mares en busca del tesoro más grande del mundo.',
     'https://cdn.myanimelist.net/images/manga/2/253146l.jpg',
     '13', 'JIKAN', NULL, 0, 8);

-- =============================================================================
-- MEDIA TAGS (asociaciones)
-- =============================================================================
INSERT INTO media_tags (media_id, tag_id) VALUES
(1, 1), (1, 2),
(2, 1),
(3, 1), (3, 2),
(6, 1),
(8, 3), (8, 4),
(10, 3),
(11, 3),
(14, 5),
(15, 5), (15, 6),
(16, 5),
(20, 7), (20, 8),
(22, 7), (22, 8),
(23, 7),
(26, 9), (26, 10),
(27, 9), (27, 10),
(30, 9),
(32, 11),
(33, 11),
(34, 11),
(36, 11),
(38, 13),
(39, 13),
(42, 14);

-- =============================================================================
-- POSTS
-- =============================================================================
INSERT INTO posts (id, content, created_at, user_id, media_item_id, linked_item_title, linked_item_type, linked_item_rating, linked_item_image_url, linked_item_creator, linked_item_release_date, linked_item_genre, linked_item_description, linked_item_album, linked_item_custom, like_count, comment_count) VALUES
(1,  'Acabo de terminar Interstellar por tercera vez y sigo llorando con la escena de los mensajes. Christopher Nolan es un visionario. Si no la habéis visto, dejad todo y ponedla YA.',
     '2026-03-15 20:30:00', 2, 1, 'Interstellar', 'PELICULA', 5,
     'https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg',
     'Christopher Nolan', '2014-11-07', 'Ciencia ficción',
     'Un grupo de exploradores viaja a través de un agujero de gusano en el espacio en un intento de asegurar la supervivencia de la humanidad.',
     NULL, 0, 4, 3),

(2,  'Clean Code debería ser lectura obligatoria en la universidad. Hay capítulos que me han cambiado completamente la forma de programar. ¿Alguna recomendación similar?',
     '2026-03-18 10:15:00', 3, 9, 'Clean Code', 'LIBRO', 4,
     'https://covers.openlibrary.org/b/id/7222246-L.jpg',
     'Robert C. Martin', '2008-08-01', 'Programación',
     'Manual de artesanía del software que enseña las mejores prácticas de codificación limpia.',
     NULL, 0, 3, 2),

(3,  'Parásitos es la película que me hizo entender que el cine coreano está a otro nivel. Cada escena está milimétricamente planificada. Bong Joon-ho merece todos los premios.',
     '2026-03-20 16:45:00', 5, 20, 'Parásitos', 'PELICULA', 5,
     'https://image.tmdb.org/t/p/w500/7IiTTgloJzvGI1TAYymCfbfl3vT.jpg',
     'Bong Joon-ho', '2019-05-30', 'Thriller, Drama',
     'La familia Kim, todos desempleados, se interesa por la adinerada familia Park y se infiltra en su hogar.',
     NULL, 0, 5, 2),

(4,  '¿Alguien más lleva toda la vida esperando "Las puertas de piedra"? Patrick Rothfuss nos debe un final. Mientras tanto, El nombre del viento sigue siendo fantasía de la buena.',
     '2026-03-22 14:00:00', 4, 15, 'El nombre del viento', 'LIBRO', 5,
     'https://covers.openlibrary.org/b/id/8269093-L.jpg',
     'Patrick Rothfuss', '2007-03-27', 'Fantasía épica',
     'Kvothe, un aventurero y músico, narra su historia desde su infancia hasta convertirse en leyenda.',
     NULL, 0, 6, 4),

(5,  'Llevo 200 horas en The Witcher 3 y acabo de descubrir una misión secundaria que no conocía. Este juego es infinito. Geralt de Rivia es vida.',
     '2026-03-25 21:00:00', 7, 32, 'The Witcher 3: Wild Hunt', 'VIDEOJUEGO', 5,
     'https://media.rawg.io/media/games/618/618c2031a07bbff6b4f611f10b6bcdbc.jpg',
     'CD Projekt RED', '2015-05-19', 'RPG, Acción',
     'Geralt de Rivia busca a su hija adoptiva mientras una fuerza sobrenatural amenaza el mundo.',
     NULL, 0, 4, 2),

(6,  'Paranoid Android de Radiohead sigue sonando como si fuera del futuro. OK Computer cumplió 29 años y el álbum que definió una generación no envejece.',
     '2026-03-28 18:30:00', 6, 27, 'Paranoid Android', 'MUSICA', 5,
     'https://e-cdns-images.dzcdn.net/images/cover/faa4b5e4e42b4e9a9345e0ab79a83c22/500x500-000000-80-0-0.jpg',
     'Radiohead', '1997-06-16', 'Rock alternativo',
     'Sencillo del tercer álbum de estudio de Radiohead, considerado uno de los mejores álbumes de la historia.',
     'OK Computer', 0, 3, 1),

(7,  'Fullmetal Alchemist: Brotherhood es el anime perfecto y no acepto debate. Historia, acción, humor, drama... lo tiene TODO. Si solo podéis ver un anime en la vida, que sea este.',
     '2026-04-01 12:00:00', 8, 38, 'Fullmetal Alchemist: Brotherhood', 'ANIME', 5,
     'https://cdn.myanimelist.net/images/anime/1208/94745l.jpg',
     'Hiromu Arakawa', '2009-04-05', 'Acción, Aventura, Fantasía',
     'Dos hermanos alquimistas buscan la piedra filosofal para restaurar sus cuerpos tras un experimento fallido.',
     NULL, 0, 5, 3),

(8,  'Acabo de terminar Hades por décima vez y el juego sigue revelando diálogos y arcos narrativos nuevos. Supergiant Games hizo algo mágico con la narrativa roguelike.',
     '2026-04-05 15:20:00', 7, 36, 'Hades', 'VIDEOJUEGO', 5,
     'https://media.rawg.io/media/games/1f4/1f47a270b8f241e4676b14d39ec620f7.jpg',
     'Supergiant Games', '2020-09-17', 'Roguelike, Acción',
     'Zagreus, hijo de Hades, intenta escapar del inframundo desafiando a dioses y monstruos griegos.',
     NULL, 0, 3, 1),

(9,  'Blade Runner 2049 es la secuela que nadie esperaba y todos necesitábamos. Villeneuve entendió perfectamente el universo de Ridley Scott y lo expandió con una belleza visual incomparable.',
     '2026-04-08 19:00:00', 5, 22, 'Blade Runner 2049', 'PELICULA', 5,
     'https://image.tmdb.org/t/p/w500/gajva2L0rPYkEWjzgFlBXCAVBE5.jpg',
     'Denis Villeneuve', '2017-10-06', 'Ciencia ficción, Drama',
     'Un nuevo blade runner descubre un secreto que podría sumir a la sociedad en el caos.',
     NULL, 0, 4, 2),

(10, 'Empezando Sapiens de Yuval Noah Harari. Las primeras páginas ya me tienen enganchada. ¿Alguien lo ha terminado? ¿Merece la pena el final?',
     '2026-04-10 09:30:00', 4, 18, 'Sapiens', 'LIBRO', NULL,
     'https://covers.openlibrary.org/b/id/8406786-L.jpg',
     'Yuval Noah Harari', '2011-01-01', 'No ficción, Historia',
     'Un recorrido por la historia de la humanidad desde la Edad de Piedra hasta el siglo XXI.',
     NULL, 0, 2, 2),

(11, 'Your Name de Makoto Shinkai es la prueba de que la animación japonesa puede hacerte llorar como ninguna película de acción real. La banda sonora de RADWIMPS es perfecta.',
     '2026-04-15 17:45:00', 8, 41, 'Your Name', 'PELICULA', 5,
     'https://image.tmdb.org/t/p/w500/q719jXXEzOoYaps6babgKnONONX.jpg',
     'Makoto Shinkai', '2016-08-26', 'Anime, Romance, Fantasía',
     'Dos adolescentes descubren que están intercambiando cuerpos misteriosamente y deciden buscarse.',
     NULL, 0, 3, 1),

(12, 'Oppenheimer + Interstellar + The Dark Knight = la santa trinidad de Nolan. No hay director actual que se le acerque. Debate abierto.',
     '2026-04-20 13:00:00', 2, 6, 'Oppenheimer', 'PELICULA', 5,
     'https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg',
     'Christopher Nolan', '2023-07-21', 'Drama, Historia',
     'La historia del físico J. Robert Oppenheimer y su papel en el desarrollo de la bomba atómica.',
     NULL, 0, 4, 3),

(13, 'Red Dead Redemption 2: ese juego donde pasas 3 horas pescando y acariciando al caballo en vez de hacer la misión principal. Y no me arrepiento de nada.',
     '2026-04-25 20:15:00', 7, 33, 'Red Dead Redemption 2', 'VIDEOJUEGO', 5,
     'https://media.rawg.io/media/games/511/5118aff5091cb3efec399c808f8c598f.jpg',
     'Rockstar Games', '2018-10-26', 'Acción, Aventura',
     'La épica historia de Arthur Morgan y la banda de Van der Linde en el ocaso del salvaje oeste americano.',
     NULL, 0, 3, 2),

(14, 'Whiplash me hizo replantearme qué significa realmente perseguir la perfección. ¿El arte justifica el sufrimiento? J.K. Simmons está aterrador.',
     '2026-05-01 11:00:00', 6, 29, 'Whiplash', 'PELICULA', 5,
     'https://image.tmdb.org/t/p/w500/7fn624j5lj3xTme2SgiLCeuedmO.jpg',
     'Damien Chazelle', '2014-10-10', 'Drama, Música',
     'Un joven baterista se inscribe en un conservatorio donde un temible instructor le empuja al límite.',
     NULL, 0, 3, 2),

(15, 'Capítulo 1100 de One Piece y Oda sigue sorprendiéndome. La saga final va a ser ÉPICA. Los que dijeron que OP era demasiado largo no saben lo que se pierden.',
     '2026-05-05 16:30:00', 8, 43, 'One Piece', 'MANGA', NULL,
     'https://cdn.myanimelist.net/images/manga/2/253146l.jpg',
     'Eiichiro Oda', '1997-07-22', 'Aventura, Acción',
     'Monkey D. Luffy y su tripulación navegan los mares en busca del tesoro más grande del mundo.',
     NULL, 0, 2, 1),

(16, 'Mr. Robot no es solo una serie sobre hacking. Es una exploración brutal de la soledad, la identidad y la lucha contra un sistema roto. Rami Malek es Elliot Alderson.',
     '2026-05-10 22:00:00', 3, 11, 'Mr. Robot', 'SERIE', 5,
     'https://image.tmdb.org/t/p/w500/oKIBhzZzDX07SoE2bOLhq2EE8rf.jpg',
     'Sam Esmail', '2015-06-24', 'Drama, Thriller',
     'Un ingeniero de ciberseguridad y hacker es reclutado por un grupo anarquista para destruir las corporaciones que maneja.',
     NULL, 0, 3, 2),

(17, 'Cerati es eterno. Te Llevo Para Que Me Lleves es un viaje sonoro que no envejece. Amor Amarillo es la canción más hermosa del rock en español.',
     '2026-05-15 14:00:00', 6, 30, 'Te Llevo Para Que Me Lleves', 'MUSICA', 5,
     'https://e-cdns-images.dzcdn.net/images/cover/3e0cb7a33efcb12f51dce5bdd4bdc5cc/500x500-000000-80-0-0.jpg',
     'Gustavo Cerati', '1993-10-04', 'Rock',
     'Canción del primer álbum solista de Gustavo Cerati, Amor Amarillo.',
     'Amor Amarillo', 0, 2, 0),

(18, 'Elden Ring me ha quitado 300 horas de vida y se las doy encantado. Cada zona es un mundo nuevo por descubrir. FromSoftware no falla.',
     '2026-05-18 19:45:00', 3, 10, 'Elden Ring', 'VIDEOJUEGO', 5,
     'https://media.rawg.io/media/games/b29/b294fdd866dcdb643e7bab370a552855.jpg',
     'FromSoftware', '2022-02-25', 'RPG, Acción',
     'Juego de rol de acción ambientado en un mundo de fantasía oscura creado por Hidetaka Miyazaki y George R.R. Martin.',
     NULL, 0, 2, 1);

-- =============================================================================
-- COMMENTS
-- =============================================================================
INSERT INTO comments (id, text, created_at, user_id, post_id, parent_comment_id) VALUES
(1,  'Totalmente de acuerdo! La banda sonora de Hans Zimmer es de otro mundo. Literalmente.', '2026-03-15 21:00:00', 3, 1, NULL),
(2,  'La escena de Miller''s Planet con la ola gigante es puro cine. Nolan es un genio.', '2026-03-15 21:30:00', 5, 1, NULL),
(3,  'A mí me parece buena pero un poco sobrevalorada, ¿no? Hay ciencia ficción mejor.', '2026-03-16 08:00:00', 7, 1, NULL),
(4,  'Te recomiendo "The Pragmatic Programmer", es el complemento perfecto!', '2026-03-18 11:00:00', 2, 2, NULL),
(5,  'Yo añadiría "Refactoring" de Martin Fowler. Entre los tres tienes la biblia del desarrollo.', '2026-03-18 12:30:00', 7, 2, NULL),
(6,  'El sótano... esa escena me dejó sin habla. Bong Joon-ho es brillante.', '2026-03-20 17:15:00', 2, 3, NULL),
(7,  'Si te gustó Parásitos, mira "Memories of Murder" del mismo director. Obra maestra.', '2026-03-20 18:00:00', 3, 3, NULL),
(8,  'Llevo esperando "Las puertas de piedra" desde 2011... Ya perdí la esperanza', '2026-03-22 14:30:00', 2, 4, NULL),
(9,  'La prosa de Rothfuss es poesía. Merece la pena aunque nunca termine la trilogía.', '2026-03-22 15:00:00', 3, 4, NULL),
(10, 'Yo lo releí hace poco y me gustó aún más que la primera vez. Kvothe es fascinante.', '2026-03-22 16:00:00', 8, 4, NULL),
(11, '@maria_gz jajaja somos muchos en ese barco! Pero la espera merece la pena, espero...', '2026-03-22 16:30:00', 4, 4, 8),
(12, 'El barón sanguinario es la mejor misión secundaria de la historia de los videojuegos. Punto.', '2026-03-25 21:30:00', 3, 5, NULL),
(13, 'Las cartas de Gwent > la trama principal. Change my mind.', '2026-03-25 22:00:00', 5, 5, NULL),
(14, 'Lucky y No Surprises en bucle toda la semana. Radiohead es terapia.', '2026-03-28 19:00:00', 2, 6, NULL),
(15, 'El sacrificio de Maes Hughes... todavía no lo supero.', '2026-04-01 12:30:00', 2, 7, NULL),
(16, 'Brotherhood > la versión del 2003. No hay debate posible.', '2026-04-01 13:00:00', 4, 7, NULL),
(17, 'Roy Mustang vs Envy es la mejor pelea del anime. CAMBIEN MI OPINIÓN.', '2026-04-01 14:00:00', 7, 7, NULL),
(18, 'Meg best girl. No acepto opiniones contrarias.', '2026-04-05 16:00:00', 8, 8, NULL),
(19, 'La fotografía de Roger Deakins merece un museo. Cada frame es un cuadro.', '2026-04-08 19:30:00', 6, 9, NULL),
(20, 'Es de esas películas que necesitas ver dos veces para apreciarla de verdad.', '2026-04-08 20:00:00', 2, 9, NULL),
(21, 'Sí, merece mucho la pena! El capítulo sobre la revolución cognitiva es brutal.', '2026-04-10 10:00:00', 3, 10, NULL),
(22, 'Yo lo terminé el mes pasado. El final te hace replantear todo. Sigue leyendo!', '2026-04-10 11:00:00', 2, 10, NULL),
(23, 'Sparkle de RADWIMPS suena en mi cabeza cada vez que veo un atardecer bonito.', '2026-04-15 18:00:00', 6, 11, NULL),
(24, 'Falta Tenet en esa lista! Aunque reconozco que es la más divisiva de Nolan.', '2026-04-20 13:30:00', 5, 12, NULL),
(25, 'Villeneuve le planta cara. Dune + Arrival + BR2049 = trilogía imbatible.', '2026-04-20 14:00:00', 3, 12, NULL),
(26, 'Nolan gang representando! Memento también merece mención.', '2026-04-20 15:00:00', 8, 12, NULL),
(27, '"I''m afraid" - Arthur Morgan. La frase que me rompió el corazón.', '2026-04-25 21:00:00', 3, 13, NULL),
(28, 'El caballo se llama Rocinante y nadie me va a decir lo contrario.', '2026-04-25 21:30:00', 5, 13, NULL),
(29, 'NOT MY TEMPO!! J.K. Simmons me da pesadillas con esa frase.', '2026-05-01 11:30:00', 5, 14, NULL),
(30, 'La escena final del concierto es la mejor secuencia musical filmada. Sin discusión.', '2026-05-01 12:00:00', 2, 14, NULL),
(31, 'Oda es el GOAT del manga y punto. La planificación a 25 años es sobrehumana.', '2026-05-05 17:00:00', 7, 15, NULL),
(32, 'El episodio 4x07 (sin cortes) es lo mejor que he visto en televisión. Punto.', '2026-05-10 22:30:00', 7, 16, NULL),
(33, 'Como programador, es la única serie que muestra el hacking de forma realista.', '2026-05-10 23:00:00', 2, 16, NULL),
(34, 'Malenia me costó 47 intentos. Pero cuando la vencí... mejor sensación gaming de mi vida.', '2026-05-18 20:15:00', 7, 18, NULL);

-- =============================================================================
-- POST LIKES
-- =============================================================================
INSERT INTO post_likes (id, post_id, user_id, liked_at) VALUES
(1,  1, 3, '2026-03-15 21:00:00'), (2,  1, 5, '2026-03-15 21:30:00'),
(3,  1, 4, '2026-03-16 09:00:00'), (4,  1, 6, '2026-03-16 10:00:00'),
(5,  2, 2, '2026-03-18 10:30:00'), (6,  2, 7, '2026-03-18 12:00:00'),
(7,  2, 4, '2026-03-18 14:00:00'),
(8,  3, 2, '2026-03-20 17:00:00'), (9,  3, 3, '2026-03-20 17:30:00'),
(10, 3, 4, '2026-03-20 18:00:00'), (11, 3, 6, '2026-03-20 19:00:00'),
(12, 3, 8, '2026-03-20 20:00:00'),
(13, 4, 2, '2026-03-22 14:30:00'), (14, 4, 3, '2026-03-22 15:00:00'),
(15, 4, 5, '2026-03-22 15:30:00'), (16, 4, 6, '2026-03-22 16:00:00'),
(17, 4, 7, '2026-03-22 17:00:00'), (18, 4, 8, '2026-03-22 18:00:00'),
(19, 5, 3, '2026-03-25 21:30:00'), (20, 5, 5, '2026-03-25 22:00:00'),
(21, 5, 8, '2026-03-26 08:00:00'), (22, 5, 2, '2026-03-26 09:00:00'),
(23, 6, 2, '2026-03-28 19:00:00'), (24, 6, 5, '2026-03-28 19:30:00'),
(25, 6, 8, '2026-03-28 20:00:00'),
(26, 7, 2, '2026-04-01 12:30:00'), (27, 7, 4, '2026-04-01 13:00:00'),
(28, 7, 5, '2026-04-01 14:00:00'), (29, 7, 6, '2026-04-01 15:00:00'),
(30, 7, 7, '2026-04-01 16:00:00'),
(31, 8, 3, '2026-04-05 16:00:00'), (32, 8, 5, '2026-04-05 17:00:00'),
(33, 8, 8, '2026-04-05 18:00:00'),
(34, 9, 2, '2026-04-08 19:30:00'), (35, 9, 3, '2026-04-08 20:00:00'),
(36, 9, 6, '2026-04-08 20:30:00'), (37, 9, 8, '2026-04-08 21:00:00'),
(38, 10, 2, '2026-04-10 10:00:00'), (39, 10, 3, '2026-04-10 11:00:00'),
(40, 11, 2, '2026-04-15 18:00:00'), (41, 11, 5, '2026-04-15 18:30:00'),
(42, 11, 6, '2026-04-15 19:00:00'),
(43, 12, 3, '2026-04-20 13:30:00'), (44, 12, 5, '2026-04-20 14:00:00'),
(45, 12, 7, '2026-04-20 15:00:00'), (46, 12, 8, '2026-04-20 16:00:00'),
(47, 13, 2, '2026-04-25 21:00:00'), (48, 13, 3, '2026-04-25 21:30:00'),
(49, 13, 5, '2026-04-25 22:00:00'),
(50, 14, 2, '2026-05-01 11:30:00'), (51, 14, 5, '2026-05-01 12:00:00'),
(52, 14, 8, '2026-05-01 13:00:00'),
(53, 15, 7, '2026-05-05 17:00:00'), (54, 15, 4, '2026-05-05 18:00:00'),
(55, 16, 2, '2026-05-10 22:30:00'), (56, 16, 5, '2026-05-10 23:00:00'),
(57, 16, 7, '2026-05-11 08:00:00'),
(58, 17, 2, '2026-05-15 14:30:00'), (59, 17, 5, '2026-05-15 15:00:00'),
(60, 18, 7, '2026-05-18 20:00:00'), (61, 18, 5, '2026-05-18 20:30:00');

-- =============================================================================
-- POST SAVES
-- =============================================================================
INSERT INTO post_saves (id, post_id, user_id, saved_at) VALUES
(1, 1, 4, '2026-03-16 09:30:00'),
(2, 2, 2, '2026-03-18 11:00:00'),
(3, 3, 8, '2026-03-20 20:30:00'),
(4, 4, 2, '2026-03-22 14:45:00'),
(5, 4, 6, '2026-03-22 16:30:00'),
(6, 5, 8, '2026-03-26 08:30:00'),
(7, 7, 2, '2026-04-01 12:45:00'),
(8, 9, 3, '2026-04-08 20:15:00'),
(9, 12, 6, '2026-04-20 14:30:00'),
(10, 14, 3, '2026-05-01 12:15:00');

-- =============================================================================
-- NOTIFICATIONS
-- =============================================================================
INSERT INTO notifications (id, recipient_id, actor_id, type, reference_id, is_read, created_at) VALUES
(1,  2, 3, 'LIKE_POST',            1,    1, '2026-03-15 21:00:00'),
(2,  2, 5, 'LIKE_POST',            1,    1, '2026-03-15 21:30:00'),
(3,  2, 3, 'COMENTARIO_POST',      1,    1, '2026-03-15 21:00:00'),
(4,  2, 5, 'COMENTARIO_POST',      1,    1, '2026-03-15 21:30:00'),
(5,  3, 2, 'LIKE_POST',            2,    1, '2026-03-18 10:30:00'),
(6,  5, 2, 'LIKE_POST',            3,    1, '2026-03-20 17:00:00'),
(7,  4, 2, 'LIKE_POST',            4,    1, '2026-03-22 14:30:00'),
(8,  4, 3, 'COMENTARIO_POST',      4,    0, '2026-03-22 15:00:00'),
(9,  7, 3, 'LIKE_POST',            5,    0, '2026-03-25 21:30:00'),
(10, 6, 2, 'LIKE_POST',            6,    0, '2026-03-28 19:00:00'),
(11, 8, 2, 'LIKE_POST',            7,    0, '2026-04-01 12:30:00'),
(12, 8, 4, 'COMENTARIO_POST',      7,    0, '2026-04-01 13:00:00'),
(13, 5, 6, 'LIKE_POST',            9,    0, '2026-04-08 20:30:00'),
(14, 5, 2, 'COMENTARIO_POST',      9,    0, '2026-04-08 20:00:00'),
(15, 2, 8, 'NUEVO_SEGUIDOR',       NULL, 0, '2026-02-15 11:00:00'),
(16, 4, 8, 'NUEVO_SEGUIDOR',       NULL, 0, '2026-02-15 11:05:00'),
(17, 6, 8, 'NUEVO_SEGUIDOR',       NULL, 0, '2026-02-15 11:10:00'),
(18, 3, 7, 'NUEVO_SEGUIDOR',       NULL, 0, '2026-02-01 10:00:00'),
(19, 5, 7, 'NUEVO_SEGUIDOR',       NULL, 0, '2026-02-01 10:05:00'),
(20, 7, 8, 'SOLICITUD_SEGUIMIENTO', NULL, 0, '2026-03-05 09:00:00'),
(21, 2, 6, 'LIKE_POST',            12,   0, '2026-04-20 14:30:00'),
(22, 3, 7, 'COMENTARIO_POST',      18,   0, '2026-05-18 20:15:00'),
(23, 6, 5, 'LIKE_POST',            17,   0, '2026-05-15 15:00:00'),
(24, 3, 5, 'LIKE_POST',            16,   0, '2026-05-10 23:00:00');

SELECT 'Script de demo ejecutado correctamente!' AS resultado;
