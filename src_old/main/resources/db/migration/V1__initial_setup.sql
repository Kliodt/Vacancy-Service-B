
--  DROP SCHEMA public CASCADE; CREATE SCHEMA public;

CREATE TABLE organization (
    id BIGSERIAL PRIMARY KEY,
    nickname VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    nickname VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    cv_link VARCHAR(512)
);

CREATE TABLE vacancy (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    long_description TEXT NOT NULL,
    min_salary INTEGER CHECK (min_salary >= 0),
    max_salary INTEGER CHECK (max_salary >= 0),
    city VARCHAR(100),
    organization_id BIGINT REFERENCES organization(id) ON DELETE CASCADE
);

CREATE TABLE user_vacancy_response (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vacancy_id BIGINT NOT NULL REFERENCES vacancy(id) ON DELETE CASCADE,
    response_date TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE user_favorites (
    list_index BIGINT NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vacancy_id BIGINT NOT NULL REFERENCES vacancy(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, vacancy_id)
);



