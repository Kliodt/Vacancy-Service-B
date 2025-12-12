CREATE TABLE vacancy (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    long_description TEXT NOT NULL,
    max_salary INTEGER CHECK (max_salary >= 0),
    min_salary INTEGER CHECK (min_salary >= 0),
    city VARCHAR(100)
);

CREATE TABLE user_vacancy_response (
    id BIGSERIAL PRIMARY KEY,
    response_date TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    user_id BIGINT NOT NULL,
    vacancy_id BIGINT NOT NULL
);

