-- Flyway migration: create table for file metadata
CREATE TABLE IF NOT EXISTS filedata (
    id BIGSERIAL PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL
);
