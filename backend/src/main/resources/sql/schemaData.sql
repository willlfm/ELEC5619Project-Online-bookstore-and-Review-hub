USE bookstore;

-- every test hash code '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm' is originally 12345678
INSERT INTO user (username, email, password_hash, name, phone, security_question, security_answer, address, city, state, postal_code, country, date_of_birth, gender, role) VALUES
            ('test_admin1', 'test_admin1@bookstore.com', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'Administrator', '13800138000', 'Where were you born?', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'No. 1 Jianguo Road, Chaoyang District, Beijing', 'Beijing', 'Beijing', '100001', 'China', '1980-01-01', 'M', 'admin'),
            ('test_user1', 'test_user1@test.com', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'Zhang San', '13900139001', 'What is your mother''s name?', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'No. 1000 Lujiazui Ring Road, Pudong New Area, Shanghai', 'Shanghai', 'Shanghai', '200120', 'China', '1990-05-15', 'M', 'normal'),
            ('test_user2', 'test_user2@example.org', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'Li Meili', '13800138002', 'What''s your favorite color?', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'No. 10 Huaxia Road, Zhujiang New Town, Tianhe District, Guangzhou', 'Guangzhou', 'Guangdong', '510623', 'China', '1985-12-25', 'F', 'normal'),
            ('test_user3', 'test_user3@gmail.com', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'Wang Wu', '13700137003', 'What is your favorite animal?', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'South Area, High-tech Park, Nanshan District, Shenzhen', 'Shenzhen', 'Guangdong', '518000', 'China', '2000-02-29', 'M', 'normal'),
            ('test_user4', 'test_user4@domain.co.uk', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'Zhao Liu', '13600136004', 'What is your father''s name?', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'No. 478 Wensan Road, Xihu District, Hangzhou', 'Hangzhou', 'Zhejiang', '310013', 'China', '1999-12-31', 'Other', 'normal'),
            ('test_user5', 'test_user5@test.cn', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'Chen Xiaoming', '13500135005', 'What is the name of your primary school?', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'No. 1388 Tianfu Avenue, High-tech Zone, Chengdu', 'Chengdu', 'Sichuan', '610041', 'China', '1995-07-07', 'M', 'normal'),
            ('test_user6', 'test_user6@verylongdomainname.com', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'Liu Xiaohong', '13400134006', 'What was your first pet''s name?', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'No. 160 Xinnan Road, Yubei District, Chongqing', 'Chongqing', 'Chongqing', '401147', 'China', '1988-04-30', 'F', 'normal'),
            ('test_user7', 'test_user7@null.com', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'Anonymous', '0123456789', 'What is your favorite number?', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', NULL, NULL, NULL, NULL, 'China', NULL, NULL, 'normal'),
            ('test_user8', 'test_user8@test.com', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'Zhou Jiu', '13300133007', 'What is your favorite food?', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'No. 180 Hanzhong Road, Gulou District, Nanjing', 'Nanjing', 'Jiangsu', '210029', 'China', '1970-01-01', 'M', 'normal'),
            ('test_user9', 'test_user9@old.com', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'Sun Shi', '13200132008', 'What''s your favorite teacher''s name?', '$2a$10$YWc6iPRY/ltaT9jcZmclbugPwXhpWCUTltZBvkDchNy.C/VrweyAm', 'No. 1 Zhongbei Road, Wuchang District, Wuhan', 'Wuhan', 'Hubei', '430071', 'China', '1975-10-10', 'F', 'normal');

INSERT INTO book (isbn, title, author, publisher, publication_date, edition, language, pages, category, description, cover_image_url, average_rating) VALUES
    ('978-7-111-54321-0', 'Thinking in Java (4th Edition)', 'Bruce Eckel', 'China Machine Press', '2007-06-01', 4, 'Chinese', 880, 'Computer Science', 'Classic Java textbook that explains Java programming concepts clearly and thoroughly.', 'thinking in java.png', 4.50),
    ('978-7-121-12345-6', 'Deep Learning with Python', 'Francois Chollet', 'Electronics Industry Press', '2018-08-01', 1, 'Chinese', 352, 'Artificial Intelligence', 'Work by the creator of Keras; a must-read for deep learning beginners.', 'deep learning with python.png', 4.80),
    ('978-1-59327-599-0', 'Automate the Boring Stuff with Python (2nd Edition)', 'Al Sweigart', 'No Starch Press', '2019-11-12', 2, 'English', 592, 'Computer Science', 'A hands-on guide to automating everyday tasks with Python.', 'Automate the Boring Stuff with Python (2nd Edition).png', 4.70),
    ('978-0-134-19044-0', 'Introduction to Algorithms', 'Thomas H. Cormen', 'MIT Press', '2009-07-31', 3, 'English', 1312, 'Computer Science', 'Classic algorithms textbook; essential reading for computer science.', 'introduction to algorithms.png', 4.70),
    ('978-7-115-50734-5', 'Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', 'Posts & Telecom Press', '2016-05-01', 1, 'Chinese', 464, 'Computer Science', 'Classic book on writing clean, maintainable code.', 'Clean Code A Handbook of Agile Software Craftsmanship.png', 4.80),
    ('978-7-121-15535-4', 'Hands-On Machine Learning with Scikit-Learn, Keras, and TensorFlow (2nd Edition)', 'Aurélien Géron', 'Electronics Industry Press', '2020-01-01', 2, 'Chinese', 800, 'Artificial Intelligence', 'A practical guide to building machine learning systems.', 'hands on ml.png', 4.75),
    ('978-7-5321-4430-6', 'Norwegian Wood', 'Haruki Murakami', 'Shanghai Translation Publishing House', '2010-05-01', 1, 'Chinese', 389, 'Literature', 'A coming-of-age story by Haruki Murakami.', 'norwegian wood.png', 4.40),
    ('978-7-02-123456-7', 'One Hundred Years of Solitude', 'Gabriel Garcia Marquez', 'People''s Literature Publishing House', '2011-06-01', 1, 'Chinese', 360, 'Literature', 'A representative work of magical realism.', 'One Hundred Years of Solitude.png', 4.90),
    ('978-7-5086-1234-5', 'Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', 'CITIC Press', '2017-02-01', 1, 'Chinese', 440, 'History', 'A history of humankind from animals to gods.', 'sapiens.png', 4.60),
    ('978-7-5442-9876-5', 'The Miracles of the Namiya General Store', 'Keigo Higashino', 'Nanhai Publishing Company', '2014-05-01', 1, 'Chinese', 291, 'Fiction', 'A warm and healing mystery novel.', 'The Miracles of the Namiya General Store.png', 4.30),
    ('978-7-115-43023-0', 'Design Patterns: Elements of Reusable Object-Oriented Software', 'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides', 'Posts & Telecom Press', '2009-01-01', 1, 'Chinese', 395, 'Computer Science', 'Classic “Gang of Four” book introducing design patterns in object-oriented software.', 'design patterns.png', 4.80),
    ('978-7-121-29899-0', 'Artificial Intelligence: A Modern Approach (4th Edition)', 'Stuart Russell, Peter Norvig', 'Electronics Industry Press', '2022-09-01', 4, 'Chinese', 1136, 'Artificial Intelligence', 'The most authoritative and widely used textbook on AI.', 'ai modern approach.png', 4.90),
    ('978-0-596-52068-7', 'JavaScript: The Good Parts', 'Douglas Crockford', 'O''Reilly Media', '2008-05-15', 1, 'English', 176, 'Computer Science', 'A concise book that identifies the elegant parts of JavaScript and how to use them effectively.', 'javascript good parts.png', 4.40),
    ('978-7-121-33045-6', 'Clean Architecture', 'Robert C. Martin', 'Electronics Industry Press', '2018-07-01', 1, 'Chinese', 432, 'Computer Science', 'A guide to software architecture and design principles.', 'clean_architecture.png', 4.75),
    ('978-7-121-36010-3', 'Effective Java (3rd Edition)', 'Joshua Bloch', 'Electronics Industry Press', '2018-05-01', 3, 'Chinese', 412, 'Computer Science', 'Best practices for Java programming.', 'effective_java.png', 4.85),
    ('978-7-121-38500-6', 'Python Crash Course', 'Eric Matthes', 'Electronics Industry Press', '2019-06-01', 2, 'Chinese', 544, 'Computer Science', 'A hands-on guide to Python programming.', 'python_crash_course.png', 4.80),
    ('978-7-121-41000-1', 'Machine Learning Yearning', 'Andrew Ng', 'Electronics Industry Press', '2018-10-01', 1, 'Chinese', 288, 'Artificial Intelligence', 'Guide to structuring machine learning projects.', 'ml_yearning.png', 4.85),
    ('978-7-121-43000-4', 'Pattern Recognition and Machine Learning', 'Christopher Bishop', 'Electronics Industry Press', '2007-08-01', 1, 'Chinese', 738, 'Artificial Intelligence', 'Comprehensive introduction to pattern recognition and ML.', 'pattern_recognition.png', 4.70),
    ('978-7-121-45000-7', 'Reinforcement Learning: An Introduction', 'Richard S. Sutton, Andrew G. Barto', 'Electronics Industry Press', '2018-11-01', 2, 'Chinese', 552, 'Artificial Intelligence', 'Fundamentals of reinforcement learning.', 'reinforcement_learning.png', 4.75),
    ('978-7-5442-0010-1', 'Pride and Prejudice', 'Jane Austen', 'People''s Literature Publishing House', '2015-06-01', 1, 'Chinese', 432, 'Literature', 'Classic English literature.', 'pride_prejudice.png', 4.90),
    ('978-7-5442-0020-2', 'Crime and Punishment', 'Fyodor Dostoevsky', 'People''s Literature Publishing House', '2014-03-01', 1, 'Chinese', 576, 'Literature', 'Russian classic novel about morality and guilt.', 'crime_punishment.png', 4.85),
    ('978-7-5442-0030-3', 'The Great Gatsby', 'F. Scott Fitzgerald', 'People''s Literature Publishing House', '2013-12-01', 1, 'Chinese', 218, 'Literature', 'A story of wealth and love in 1920s America.', 'great_gatsby.png', 4.80),
    ('978-7-5086-0010-1', 'Guns, Germs, and Steel', 'Jared Diamond', 'CITIC Press', '2012-05-01', 1, 'Chinese', 528, 'History', 'Explains why some civilizations advanced faster than others.', 'guns_germs_steel.png', 4.70),
    ('978-7-5086-0020-2', 'A History of the World in 100 Objects', 'Neil MacGregor', 'CITIC Press', '2011-09-01', 1, 'Chinese', 400, 'History', 'Historical artifacts as windows into world history.', 'history_100_objects.png', 4.60),
    ('978-7-5086-0030-3', 'The Silk Roads', 'Peter Frankopan', 'CITIC Press', '2016-08-01', 1, 'Chinese', 672, 'History', 'A new history of the world focusing on the Silk Road.', 'silk_roads.png', 4.75),
    ('978-7-5442-0040-4', 'Kafka on the Shore', 'Haruki Murakami', 'Shanghai Translation Publishing House', '2010-05-01', 1, 'Chinese', 505, 'Fiction', 'Magical realism novel with mysterious plot.', 'kafka_shore.png', 4.80),
    ('978-7-5442-0050-5', 'The Catcher in the Rye', 'J.D. Salinger', 'People''s Literature Publishing House', '2012-03-01', 1, 'Chinese', 277, 'Fiction', 'Coming-of-age story of Holden Caulfield.', 'catcher_rye.png', 4.75),
    ('978-7-5442-0060-6', 'The Hobbit', 'J.R.R. Tolkien', 'People''s Literature Publishing House', '2011-06-01', 1, 'Chinese', 310, 'Fiction', 'Fantasy novel about the journey of Bilbo Baggins.', 'hobbit.png', 4.85),
    ('978-7-5086-0040-4', 'The Lean Startup', 'Eric Ries', 'CITIC Press', '2012-09-01', 1, 'Chinese', 336, 'Economics', 'Innovative methods for startups.', 'lean startup.png', 4.70),
    ('978-7-5086-0050-5', 'Zero to One', 'Peter Thiel', 'CITIC Press', '2014-09-01', 1, 'Chinese', 224, 'Economics', 'Notes on startups and building the future.', 'zero to one.png', 4.65),
    ('978-7-5086-0060-6', 'Good to Great', 'Jim Collins', 'CITIC Press', '2001-10-01', 1, 'Chinese', 320, 'Economics', 'Why some companies make the leap to greatness.', 'good_to_great.png', 4.75),
    ('978-7-5442-0070-7', 'Thinking, Fast and Slow', 'Daniel Kahneman', 'People''s Literature Publishing House', '2012-04-01', 1, 'Chinese', 512, 'Psychology', 'Insight into human thinking patterns.', 'thinking_fast_slow.png', 4.85),
    ('978-7-5442-0080-8', 'Emotional Intelligence', 'Daniel Goleman', 'People''s Literature Publishing House', '1996-03-01', 1, 'Chinese', 384, 'Psychology', 'Why emotional intelligence matters.', 'emotional_intelligence.png', 4.70),
    ('978-7-5442-0090-9', 'Mindset: The New Psychology of Success', 'Carol S. Dweck', 'People''s Literature Publishing House', '2006-02-01', 1, 'Chinese', 320, 'Psychology', 'Growth mindset versus fixed mindset.', 'mindset.png', 4.75),
    ('978-7-5442-0100-0', 'Steal Like an Artist', 'Austin Kleon', 'People''s Literature Publishing House', '2012-08-01', 1, 'Chinese', 176, 'Art', '10 transformative principles for creativity.', 'steal_artist.png', 4.70),
    ('978-7-5442-0110-1', 'The Art of Color', 'Johannes Itten', 'People''s Literature Publishing House', '1961-01-01', 1, 'Chinese', 336, 'Art', 'Classic book on color theory.', 'art_of_color.png', 4.65),
    ('978-7-5442-0120-2', 'Interaction of Color', 'Josef Albers', 'People''s Literature Publishing House', '1963-01-01', 1, 'Chinese', 240, 'Art', 'Fundamental book on color interactions.', 'interaction_color.png', 4.60),
    ('978-7-5442-0130-3', 'Meditations', 'Marcus Aurelius', 'People''s Literature Publishing House', '2006-05-01', 1, 'Chinese', 256, 'Philosophy', 'Classic stoic reflections.', 'meditations.png', 4.85),
    ('978-7-5442-0140-4', 'The Republic', 'Plato', 'People''s Literature Publishing House', '2009-03-01', 1, 'Chinese', 400, 'Philosophy', 'Plato''s dialogue on justice and the ideal state.', 'republic.png', 4.80),
    ('978-7-5442-0150-5', 'Beyond Good and Evil', 'Friedrich Nietzsche', 'People''s Literature Publishing House', '2009-06-01', 1, 'Chinese', 320, 'Philosophy', 'Exploration of morality and philosophy.', 'beyond_good_evil.png', 4.70),
    ('978-7-121-50000-1', 'Gray''s Anatomy (41st Edition)', 'Henry Gray', 'Electronics Industry Press', '2015-06-01', 41, 'Chinese', 1600, 'Medicine', 'Definitive reference for human anatomy.', 'grays_anatomy.png', 4.85),
    ('978-7-121-51000-2', 'The Emperor of All Maladies', 'Siddhartha Mukherjee', 'Electronics Industry Press', '2011-11-01', 1, 'Chinese', 608, 'Medicine', 'A biography of cancer.', 'emperor_maladies.png', 4.90),
    ('978-7-121-52000-3', 'How Not to Die', 'Michael Greger', 'Electronics Industry Press', '2015-05-01', 1, 'Chinese', 576, 'Health', 'Scientific guide to healthy living and longevity.', 'how_not_die.png', 4.75);

INSERT INTO book_format (book_id, format, price, stock_quantity, reserved_quantity, total_sales, source_url) VALUES
    (1, 'paperback', 108.00, 50, 5, 1000, NULL),
    (1, 'ebook', 10.00, 50, 5, 1000, 'Thinking_in_Java(4th).txt'),
    (2, 'paperback', 168.00, 30, 10, 500, NULL),
    (3, 'paperback', 220.00, 40, 2, 1200, NULL),
    (4, 'paperback', 580.00, 20, 0, 200, NULL),
    (5, 'paperback', 88.00, 60, 5, 3000, NULL),
    (6, 'paperback', 198.00, 70, 8, 1800, NULL),
    (7, 'ebook', 45.00, 25, 3, 2200, 'Norwegian_Wood.txt'),
    (8, 'paperback', 55.00, 0, 0, 2000, NULL),
    (9, 'paperback', 68.00, 200, 20, 1500, NULL),
    (10, 'paperback', 39.50, 150, 15, 800, NULL),
    (11, 'paperback', 85.00, 40, 5, 2500, NULL),
    (12, 'ebook', 268.00, 35, 2, 1500, 'Artificial_Intelligence_A_Modern_Approach(4th).txt'),
    (13, 'paperback', 180.00, 60, 10, 2200, NULL),
    (14, 'paperback', 99.00, 40, 5, 1200, NULL),
    (15, 'paperback', 108.00, 50, 10, 2000, NULL),
    (16, 'paperback', 95.00, 60, 5, 1500, NULL),
    (17, 'ebook', 78.00, 70, 8, 1800, 'Machine_Learning_Yearning.docx'),
    (18, 'paperback', 150.00, 30, 2, 900, NULL),
    (19, 'paperback', 120.00, 40, 5, 1200, NULL),
    (20, 'paperback', 42.00, 60, 10, 3500, NULL),
    (21, 'paperback', 55.00, 40, 5, 2800, NULL),
    (22, 'paperback', 38.00, 70, 15, 3200, NULL),
    (23, 'ebook', 78.00, 50, 5, 2100, 'Guns_Germs_and_Steel.pdf'),
    (24, 'ebook', 68.00, 30, 2, 1500, 'A_History_of_the_World_in_100_Objects.zip'),
    (25, 'ebook', 88.00, 40, 5, 1800, 'The_Silk_Roads.txt'),
    (26, 'ebook', 58.00, 60, 10, 2300, 'Kafka_on_the_Shore.txt'),
    (27, 'ebook', 45.00, 50, 5, 2100, 'The_Catcher_in_the_Rye.txt'),
    (28, 'ebook', 50.00, 70, 10, 2500, 'The_Hobbit.txt'),
    (29, 'paperback', 65.00, 40, 5, 1900, NULL),
    (30, 'paperback', 55.00, 35, 3, 1700, NULL),
    (31, 'paperback', 78.00, 50, 5, 2100, NULL),
    (32, 'paperback', 68.00, 50, 5, 2200, NULL),
    (33, 'paperback', 58.00, 40, 5, 1800, NULL),
    (34, 'paperback', 60.00, 45, 5, 2000, NULL),
    (35, 'paperback', 42.00, 60, 10, 1500, NULL),
    (36, 'paperback', 88.00, 40, 5, 1300, NULL),
    (37, 'paperback', 78.00, 35, 5, 1200, NULL),
    (38, 'paperback', 38.00, 50, 5, 2000, NULL),
    (39, 'paperback', 48.00, 40, 5, 1800, NULL),
    (40, 'paperback', 55.00, 35, 5, 1600, NULL),
    (41, 'paperback', 680.00, 20, 2, 1200, NULL),
    (42, 'paperback', 128.00, 30, 3, 1500, NULL),
    (43, 'paperback', 98.00, 40, 5, 1700, NULL);


-- Reservation data with correct format: user_id, book_id, reservation_date, time_slot, reserved_date, expiry_date, status
INSERT INTO reservation (user_id, book_id, reservation_date, time_slot, reserved_date, expiry_date, status, created_at) VALUES
    -- User 2: Has 2 reserved orders (within limit)
    (2, 1, '2025-10-18', '08:00-10:00', '2025-10-17 03:47:00', '2025-10-18 18:00:00', 'reserved', '2025-10-17 03:47:00'),
    (2, 3, '2025-10-19', '10:00-12:00', '2025-10-17 04:10:00', '2025-10-19 18:00:00', 'reserved', '2025-10-17 04:10:00'),
    
    -- User 3: Has 1 picked, 1 returned (both don't count towards limit)
    (3, 2, '2025-10-17', '08:00-10:00', '2025-10-16 09:15:00', '2025-10-17 18:00:00', 'picked', '2025-10-16 09:15:00'),
    (3, 4, '2025-10-15', '14:00-16:00', '2025-10-14 11:20:00', '2025-10-15 18:00:00', 'returned', '2025-10-14 11:20:00'),
    
    -- User 4: Has 3 reserved orders (at limit)
    (4, 5, '2025-10-20', '08:00-10:00', '2025-10-17 08:00:00', '2025-10-20 18:00:00', 'reserved', '2025-10-17 08:00:00'),
    (4, 6, '2025-10-21', '12:00-14:00', '2025-10-17 08:15:00', '2025-10-21 18:00:00', 'reserved', '2025-10-17 08:15:00'),
    (4, 7, '2025-10-22', '14:00-16:00', '2025-10-17 08:30:00', '2025-10-22 18:00:00', 'reserved', '2025-10-17 08:30:00'),
    
    -- User 5: Has cancelled reservations
    (5, 8, '2025-10-18', '10:00-12:00', '2025-10-16 15:00:00', '2025-10-18 18:00:00', 'reservation_cancelled', '2025-10-16 15:00:00'),
    (5, 9, '2025-10-19', '08:00-10:00', '2025-10-17 09:00:00', '2025-10-19 18:00:00', 'reserved', '2025-10-17 09:00:00'),
    
    -- User 6: Has 1 warning status (admin can change to returned)
    (6, 10, '2025-10-16', '14:00-16:00', '2025-10-15 13:20:00', '2025-10-16 18:00:00', 'warning', '2025-10-15 13:20:00'),
    (6, 11, '2025-10-20', '10:00-12:00', '2025-10-17 10:00:00', '2025-10-20 18:00:00', 'reserved', '2025-10-17 10:00:00'),
    
    -- User 7: Has returned books
    (7, 12, '2025-10-10', '08:00-10:00', '2025-10-09 08:00:00', '2025-10-10 18:00:00', 'returned', '2025-10-09 08:00:00'),
    (7, 13, '2025-10-23', '16:00-18:00', '2025-10-17 11:00:00', '2025-10-23 18:00:00', 'reserved', '2025-10-17 11:00:00'),
    
    -- User 8: Mixed statuses
    (8, 14, '2025-10-17', '10:00-12:00', '2025-10-16 12:30:00', '2025-10-17 18:00:00', 'picked', '2025-10-16 12:30:00'),
    (8, 15, '2025-10-21', '08:00-10:00', '2025-10-17 12:00:00', '2025-10-21 18:00:00', 'reserved', '2025-10-17 12:00:00'),
    
    -- User 9: Has cancelled
    (9, 16, '2025-10-18', '14:00-16:00', '2025-10-17 14:00:00', '2025-10-18 18:00:00', 'reservation_cancelled', '2025-10-17 14:00:00'),
    
    -- User 10: Normal reservations
    (10, 17, '2025-10-19', '12:00-14:00', '2025-10-17 15:00:00', '2025-10-19 18:00:00', 'reserved', '2025-10-17 15:00:00'),
    (10, 18, '2025-10-24', '08:00-10:00', '2025-10-17 15:15:00', '2025-10-24 18:00:00', 'reserved', '2025-10-17 15:15:00');

INSERT INTO review (user_id, book_id, rating, comment, created_at) VALUES
           (2, 1, 5, 'Excellent Java beginner book; very detailed. Recommended for all beginners!', '2024-08-15 10:30:00'),
           (3, 2, 4, 'Good book on deep learning, but some translations are inaccurate.', '2024-08-20 14:15:00'),
           (4, 8, 5, 'Márquez''s classic; the pinnacle of magical realism!', '2024-07-25 09:45:00'),
           (5, 9, 3, 'It''s okay, but not as exciting as expected.', '2024-07-10 16:20:00'),
           (7, 4, 5, 'The bible of algorithm learning; worth reading repeatedly.', '2024-06-15 13:30:00'),
           (2, 3, 2, 'Content is too simple; not suitable for experienced developers.', '2024-08-25 15:45:00'),
           (6, 10, 1, 'Completely incomprehensible; I don''t know what it''s about.', '2024-06-30 11:00:00'),
           (8, 7, 4, 'Book with NULL ISBN, but the content is pretty good.', '2024-05-20 12:00:00'),
           (9, 6, 1, 'Book with a negative price; quality is also poor!', '2024-04-15 18:30:00'),
           (10, 5, 3, 'Book with an excessively long title; content is mediocre.', '2024-03-10 14:20:00');

INSERT INTO cart_item (user_id, book_id, format, quantity, added_at) VALUES
            (1, 1, 'paperback', 2, '2024-09-01 10:05:00'),
            (1, 2, 'paperback', 1, '2024-09-01 10:10:00'),
            (1, 8, 'paperback', 3, '2024-09-20 09:30:00'),
            (2, 3, 'paperback', 1, '2024-08-15 09:30:00'),
            (2, 9, 'paperback', 2, '2024-08-16 10:15:00'),
            (3, 10, 'paperback', 1, '2024-09-10 14:30:00'),
            (4, 4, 'paperback', 1, '2024-07-01 11:30:00'),
            (5, 5, 'paperback', 5, '2024-09-15 13:30:00'),
            (6, 6, 'paperback', 1, '2024-06-01 08:30:00'),
            (7, 7, 'paperback', 2, '2024-05-10 12:30:00');

INSERT INTO `order` (user_id, order_date, total_amount, status, created_at, updated_at, shipping_name, shipping_address, shipping_city, shipping_postcode, shipping_country) VALUES
            (2, '2024-09-15 10:30:00', 324.00, 'completed', '2024-09-15 10:30:00', '2024-09-16 14:20:00','Alice', '123 Main St', 'Sydney', '2000', 'Australia'),
            (3, '2024-08-20 14:45:00', 168.00, 'shipped', '2024-08-20 14:45:00', '2024-08-22 09:15:00','Bob', '45 Queen St', 'Melbourne', '3000', 'Australia'),
            (4, '2024-09-12 16:00:00', 55.00, 'pending', '2024-09-12 16:00:00', '2024-09-12 16:00:00','Charlie', '78 King Rd', 'Brisbane', '4000', 'Australia'),
            (5, '2024-07-05 11:30:00', 136.00, 'cancelled', '2024-07-05 11:30:00', '2024-07-06 10:00:00','David', '22 Park Ave', 'Perth', '6000', 'Australia'),
            (6, '2024-09-18 09:15:00', 39.50, 'processing', '2024-09-18 09:15:00', '2024-09-18 10:30:00','Eva', '11 Lake St', 'Adelaide', '5000', 'Australia'),
            (7, '2024-06-25 13:20:00', 580.00, 'completed', '2024-06-25 13:20:00', '2024-06-28 16:45:00','Frank', '88 Hill Rd', 'Sydney', '2000', 'Australia'),
            (8, '2024-05-15 12:00:00', 0.01, 'completed', '2024-05-15 12:00:00', '2024-05-15 12:05:00','Grace', '5 River St', 'Melbourne', '3000', 'Australia'),
            (9, '2024-04-10 18:45:00', 10.00, 'pending', '2024-04-10 18:45:00', '2024-04-10 18:45:00','Henry', '77 Ocean Blvd', 'Brisbane', '4000', 'Australia'),
            (2, '2024-09-20 15:30:00', 216.00, 'shipped', '2024-09-20 15:30:00', '2024-09-21 08:00:00','Alice', '123 Main St', 'Sydney', '2000', 'Australia'),
            (10, '2024-03-20 14:00:00', 118.50, 'completed', '2024-03-20 14:00:00', '2024-03-25 11:30:00','Ivy', '99 Sunset Rd', 'Perth', '6000', 'Australia');



INSERT INTO order_item (order_id, book_format_id, quantity, price) VALUES
            (1, 1, 2, 108.00),
            (1, 2, 1, 108.00),
            (2, 2, 1, 168.00),
            (3, 8, 1, 55.00),
            (4, 9, 2, 68.00),
            (5, 10, 1, 39.50),
            (6, 4, 1, 580.00),
            (7, 5, 1, 0.01),
            (8, 6, 1, -10.00),
            (9, 1, 2, 108.00);

INSERT INTO payment (order_id, payment_method, payment_date, amount, status, transaction_id, paypal_order_id) VALUES
            (1, 'paypal', '2024-09-15 10:35:00', 324.00, 'completed', 'TXN20240915001', 'TXN20240915001'),
            (2, 'paypal', '2024-08-20 14:50:00', 168.00, 'completed', 'TXN20240820002', 'TXN20240915001'),
            (3, 'paypal', '2024-09-12 16:05:00', 55.00, 'pending', 'TXN20240912003', 'TXN20240915001'),
            (4, 'paypal', '2024-07-05 11:35:00', 136.00, 'failed', 'TXN20240705004', 'TXN20240915001'),
            (5, 'paypal', '2024-09-18 09:20:00', 39.50, 'processing', 'TXN20240918005', 'TXN20240915001'),
            (6, 'paypal', '2024-06-25 13:25:00', 580.00, 'completed', 'TXN20240625006', 'TXN20240915001'),
            (7, 'paypal', '2024-05-15 12:05:00', 0.01, 'completed', 'TXN20240515007', 'TXN20240915001'),
            (8, 'paypal', '2024-04-10 18:50:00', -10.00, 'failed', 'TXN20240410008', 'TXN20240915001'),
            (9, 'paypal', '2024-09-20 15:35:00', 216.00, 'completed', 'TXN20240920009', 'TXN20240915001'),
            (10, 'paypal', '2024-03-20 14:05:00', 118.50, 'completed', 'TXN20240320010', 'TXN20240915001');

INSERT INTO refund (payment_id, order_id, refund_date, amount, reason, status, processed_at) VALUES
            (4, 4, '2024-07-06 10:00:00', 136.00, 'Order cancelled, full refund', 'completed', '2024-07-07 09:30:00'),
            (8, 8, '2024-04-11 09:15:00', -10.00, 'Payment failed, automatic refund', 'completed', '2024-04-11 10:00:00'),
            (1, 1, '2024-09-16 14:20:00', 108.00, 'Product quality issue, partial refund', 'completed', '2024-09-17 11:00:00'),
            (2, 2, '2024-08-22 09:15:00', 50.00, 'Compensation for delayed shipping', 'completed', '2024-08-22 10:30:00'),
            (3, 3, '2024-09-13 10:00:00', 55.00, 'Customer requested a refund', 'pending', NULL),
            (5, 5, '2024-09-19 11:30:00', 20.00, 'Slight product damage', 'processing', '2024-09-19 12:00:00'),
            (6, 6, '2024-06-29 16:45:00', 100.00, 'Return without reason', 'rejected', '2024-06-30 09:00:00'),
            (7, 7, '2024-05-16 08:00:00', 0.01, 'Test refund minimal amount', 'completed', '2024-05-16 08:30:00'),
            (9, 9, '2024-09-21 08:00:00', 216.00, 'Customer changed their mind', 'pending', NULL),
            (10, 10, '2024-03-25 11:30:00', 59.25, 'Item not as described', 'completed', '2024-03-26 10:15:00');

INSERT INTO feedback (user_id, description, created_at, status, response, responded_at) VALUES
            (2, 'The website loads too slowly; please optimize it!', '2024-09-15 15:30:00', 'open', NULL, NULL),
            (3, 'Search is not accurate enough; often cannot find the desired book.', '2024-08-20 16:45:00', 'in_progress', 'We are improving the search algorithm', '2024-08-21 09:00:00'),
            (4, 'The ISBN information of this book is incorrect; please verify.', '2024-09-10 11:20:00', 'resolved', 'Thanks for your feedback; we have corrected the ISBN information', '2024-09-11 14:30:00'),
            (5, 'Please add more payment methods.', '2024-07-05 13:15:00', 'open', NULL, NULL),
            (6, 'Order status updates are not timely; display issues exist.', '2024-09-18 10:00:00', 'resolved', 'The system has been fixed; status updates are now normal', '2024-09-18 15:45:00'),
            (7, 'This book shows a negative price; obviously a bug!', '2024-06-25 17:30:00', 'resolved', 'Thanks for the reminder; we have fixed the price display issue', '2024-06-26 11:00:00'),
            (8, 'NULL value handling has issues; many places display incorrectly.', '2024-05-15 14:20:00', 'in_progress', 'We are conducting a comprehensive check on NULL value handling issues', '2024-05-16 16:00:00'),
            (9, 'Excessively long book titles are displayed truncated.', '2024-04-10 12:45:00', 'open', NULL, NULL),
            (10, 'Email verification during registration is not working.', '2024-03-20 09:30:00', 'resolved', 'Email verification has been fixed', '2024-03-21 10:15:00'),
            (2, 'Items in the shopping cart can have negative quantities; this is a serious bug!', '2024-09-20 18:00:00', 'open', NULL, NULL);
