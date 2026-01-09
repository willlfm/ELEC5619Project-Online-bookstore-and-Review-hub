create schema if not exists bookstore;
use bookstore;

CREATE TABLE IF NOT EXISTS user (
                                    user_id INT PRIMARY KEY AUTO_INCREMENT,
                                    username VARCHAR(50) NOT NULL UNIQUE,
                                    email VARCHAR(100) NOT NULL UNIQUE,
                                    password_hash VARCHAR(255) NOT NULL,
                                    name VARCHAR(50) NOT NULL,
                                    phone VARCHAR(20) NOT NULL,
                                    security_question VARCHAR(255) NOT NULL,
                                    security_answer VARCHAR(255) NOT NULL,
                                    address TEXT,
                                    city VARCHAR(50),
                                    state VARCHAR(50),
                                    postal_code VARCHAR(20),
                                    country VARCHAR(50) DEFAULT 'China',
                                    date_of_birth DATE,
                                    gender ENUM('M', 'F', 'Other'),
                                    role ENUM('admin', 'normal') DEFAULT 'normal',
                                    INDEX idx_username (username),
                                    INDEX idx_email (email)
);

CREATE TABLE IF NOT EXISTS book (
                                    book_id INT PRIMARY KEY AUTO_INCREMENT,
                                    isbn VARCHAR(20) UNIQUE,
                                    title VARCHAR(200) NOT NULL,
                                    author VARCHAR(100) NOT NULL,
                                    publisher VARCHAR(100) NOT NULL,
                                    publication_date DATE,
                                    edition INT DEFAULT 1,
                                    language VARCHAR(20) DEFAULT 'Chinese',
                                    pages INT,
                                    category VARCHAR(50),
                                    description TEXT,
                                    cover_image_url VARCHAR(500),
                                    average_rating DECIMAL(3,2) DEFAULT 0.00,
                                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    INDEX idx_isbn (isbn),
                                    INDEX idx_title (title),
                                    INDEX idx_author (author),
                                    INDEX idx_publisher (publisher),
                                    INDEX idx_category (category),
                                    INDEX idx_average_rating (average_rating),
                                    FULLTEXT idx_search (title, author, description)
);

CREATE TABLE IF NOT EXISTS book_format (
                                           book_format_id INT PRIMARY KEY AUTO_INCREMENT,
                                           book_id INT NOT NULL,
                                           format ENUM('paperback', 'ebook') NOT NULL,
                                           price DECIMAL(8,2) NOT NULL,
                                           stock_quantity INT NOT NULL DEFAULT 0,
                                           reserved_quantity INT DEFAULT 0,
                                           total_sales INT DEFAULT 0,
                                           source_url VARCHAR(500),
                                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                           updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                           UNIQUE KEY uq_book_format (book_id, format),
                                           FOREIGN KEY (book_id) REFERENCES book(book_id) ON DELETE CASCADE,
                                           INDEX idx_format (format),
                                           INDEX idx_price (price)
);


CREATE TABLE IF NOT EXISTS reservation (
                                           reservation_id INT PRIMARY KEY AUTO_INCREMENT,
                                           user_id INT NOT NULL,
                                           book_id INT NOT NULL,
                                           reservation_date DATE NOT NULL,
                                           time_slot VARCHAR(20) NOT NULL,
                                           reserved_date DATETIME NOT NULL,
                                           expiry_date DATETIME NOT NULL,
                                           status VARCHAR(30) NOT NULL DEFAULT 'reserved',
                                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                           INDEX idx_user_id (user_id),
                                           INDEX idx_book_id (book_id),
                                           INDEX idx_status (status),
                                           INDEX idx_reservation_date (reservation_date)
);

CREATE TABLE IF NOT EXISTS review (
                                      review_id INT PRIMARY KEY AUTO_INCREMENT,
                                      user_id INT NOT NULL,
                                      book_id INT NOT NULL,
                                      rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
                                      comment TEXT,
                                      status VARCHAR(20) NOT NULL DEFAULT 'normal',
                                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                      INDEX idx_user_id (user_id),
                                      INDEX idx_book_id (book_id),
                                      INDEX idx_rating (rating)
);

CREATE TABLE IF NOT EXISTS cart_item (
                                         cart_item_id INT PRIMARY KEY AUTO_INCREMENT,
                                         user_id INT NOT NULL,
                                         book_id INT NOT NULL,
                                         format ENUM('paperback', 'ebook') NOT NULL,
                                         quantity INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
                                         added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                         FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
                                         FOREIGN KEY (book_id) REFERENCES book(book_id) ON DELETE CASCADE,
                                         UNIQUE KEY uq_user_book_format (user_id, book_id, format)
);

CREATE TABLE IF NOT EXISTS `order` (
                                       order_id INT PRIMARY KEY AUTO_INCREMENT,
                                       user_id INT NOT NULL,
                                       order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       total_amount DECIMAL(10,2) NOT NULL,
                                       shipping_name VARCHAR(100),
                                       shipping_address TEXT,
                                       shipping_city VARCHAR(50),
                                       shipping_postcode VARCHAR(20),
                                       shipping_country VARCHAR(50),
                                       status VARCHAR(20) NOT NULL DEFAULT 'pending',
                                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       INDEX idx_user_id (user_id),
                                       INDEX idx_status (status),
                                       INDEX idx_order_date (order_date)
);

CREATE TABLE IF NOT EXISTS order_item (
                                          order_item_id INT PRIMARY KEY AUTO_INCREMENT,
                                          order_id INT NOT NULL,
                                          book_format_id INT NOT NULL,
                                          quantity INT NOT NULL CHECK (quantity > 0),
                                          price DECIMAL(8,2) NOT NULL,
                                          FOREIGN KEY (order_id) REFERENCES `order`(order_id) ON DELETE CASCADE,
                                          FOREIGN KEY (book_format_id) REFERENCES book_format(book_format_id) ON DELETE CASCADE,
                                          INDEX idx_order_id (order_id),
                                          INDEX idx_book_id (book_format_id)
);

CREATE TABLE IF NOT EXISTS payment (
                                       payment_id INT PRIMARY KEY AUTO_INCREMENT,
                                       order_id INT NOT NULL,
                                       payment_method VARCHAR(50) NOT NULL,
                                       payment_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       amount DECIMAL(10,2) NOT NULL,
                                       status VARCHAR(20) NOT NULL DEFAULT 'pending',
                                       transaction_id VARCHAR(100),
                                       paypal_order_id VARCHAR(100),
                                       FOREIGN KEY (order_id) REFERENCES `order`(order_id) ON DELETE CASCADE,
                                       INDEX idx_order_id (order_id),
                                       INDEX idx_status (status),
                                       INDEX idx_payment_date (payment_date),
                                       INDEX idx_transaction_id (transaction_id),
                                       INDEX idx_paypal_order_id (paypal_order_id)
);

CREATE TABLE IF NOT EXISTS refund (
                                      refund_id INT PRIMARY KEY AUTO_INCREMENT,
                                      payment_id INT NOT NULL,
                                      order_id INT NOT NULL,
                                      refund_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      amount DECIMAL(10,2) NOT NULL,
                                      reason VARCHAR(255),
                                      status VARCHAR(20) NOT NULL DEFAULT 'pending',
                                      processed_at DATETIME NULL,
                                      FOREIGN KEY (payment_id) REFERENCES payment(payment_id) ON DELETE CASCADE,
                                      FOREIGN KEY (order_id) REFERENCES `order`(order_id) ON DELETE CASCADE,
                                      INDEX idx_payment_id (payment_id),
                                      INDEX idx_order_id (order_id),
                                      INDEX idx_status (status)
);

CREATE TABLE IF NOT EXISTS feedback (
                                        feedback_id INT PRIMARY KEY AUTO_INCREMENT,
                                        user_id INT NOT NULL,
                                        description TEXT NOT NULL,
                                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                        status VARCHAR(20) NOT NULL DEFAULT 'open',
                                        response TEXT NULL,
                                        responded_at DATETIME NULL,
                                        INDEX idx_user_id (user_id),
                                        INDEX idx_status (status),
                                        INDEX idx_created_at (created_at)
);

ALTER TABLE reservation ADD FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE;
ALTER TABLE review ADD FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE;
ALTER TABLE cart_item ADD FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE;
ALTER TABLE `order` ADD FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE;
ALTER TABLE feedback ADD FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE;

ALTER TABLE reservation ADD FOREIGN KEY (book_id) REFERENCES book(book_id) ON DELETE CASCADE;
ALTER TABLE review ADD FOREIGN KEY (book_id) REFERENCES book(book_id) ON DELETE CASCADE;
ALTER TABLE cart_item ADD FOREIGN KEY (book_id) REFERENCES book(book_id) ON DELETE CASCADE;
ALTER TABLE order_item ADD FOREIGN KEY (book_format_id) REFERENCES book(book_id) ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS notification (
                                            notification_id INT PRIMARY KEY AUTO_INCREMENT,
                                            user_id INT,
                                            order_id INT,
                                            title VARCHAR(150) NOT NULL,
                                            message TEXT NOT NULL,
                                            type VARCHAR(30),
                                            pinned BOOLEAN NOT NULL DEFAULT FALSE,
                                            is_read BOOLEAN NOT NULL DEFAULT FALSE,
                                            status VARCHAR(30) DEFAULT 'active',
                                            created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            INDEX idx_user_id (user_id),
                                            INDEX idx_order_id (order_id),
                                            INDEX idx_created_at (created_at),
                                            FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
                                            FOREIGN KEY (order_id) REFERENCES `order`(order_id) ON DELETE CASCADE
);