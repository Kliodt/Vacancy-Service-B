CREATE TABLE IF NOT EXISTS vacancy (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    salary INTEGER CHECK (salary >= 0),
    city VARCHAR(100),
    organization_id BIGINT
);

CREATE TABLE IF NOT EXISTS user_vacancy_response (
    id BIGSERIAL PRIMARY KEY,
    response_date TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    user_id BIGINT NOT NULL,
    vacancy_id BIGINT NOT NULL
);

