-- =============================================
-- Database Schema
-- =============================================

-- -----------------------------------------
-- Core User table
-- -----------------------------------------
CREATE TABLE users (
                       username          VARCHAR(50)    NOT NULL PRIMARY KEY,
                       password_hash     CHAR(64)       NOT NULL,
                       nickname          VARCHAR(30)    NOT NULL,
                       email             VARCHAR(100)   NOT NULL,
                       gender            ENUM('male','female') NOT NULL,
                       difficulty_level  INT            NOT NULL DEFAULT 3,
                       coins             INT            NOT NULL DEFAULT 0,
                       diamonds          INT            NOT NULL DEFAULT 0,
                       pots              INT            NOT NULL DEFAULT 0,
                       security_question VARCHAR(200)   NOT NULL,
                       security_answer   VARCHAR(200)   NOT NULL,
                       highest_score     INT            NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------
-- User's unlocked plants with upgrade level
-- -----------------------------------------
CREATE TABLE user_plants (
                             username     VARCHAR(50) NOT NULL,
                             plant_type   VARCHAR(30) NOT NULL,
                             upgrade_level INT        NOT NULL DEFAULT 1,
                             PRIMARY KEY (username, plant_type),
                             FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------
-- Zombies the user has seen (unlocked)
-- -----------------------------------------
CREATE TABLE user_seen_zombies (
                                   username    VARCHAR(50) NOT NULL,
                                   zombie_type VARCHAR(30) NOT NULL,
                                   PRIMARY KEY (username, zombie_type),
                                   FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------
-- Levels completed by the user
-- -----------------------------------------
CREATE TABLE user_completed_levels (
                                       username      VARCHAR(50) NOT NULL,
                                       chapter_name  VARCHAR(50) NOT NULL,
                                       level_number  INT         NOT NULL,
                                       PRIMARY KEY (username, chapter_name, level_number),
                                       FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------
-- News feed items
-- -----------------------------------------
CREATE TABLE news (
                      id          INT AUTO_INCREMENT PRIMARY KEY,
                      title       VARCHAR(100)  NOT NULL,
                      message     TEXT          NOT NULL,
                      created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------
-- Last seen news ID per user
-- -----------------------------------------
CREATE TABLE user_last_seen_news (
                                     username           VARCHAR(50) NOT NULL PRIMARY KEY,
                                     last_seen_news_id  INT         NOT NULL DEFAULT 0,
                                     FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
                                     FOREIGN KEY (last_seen_news_id) REFERENCES news(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
