CREATE TABLE IF NOT EXISTS file_object (
    uuid VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE,
    mime VARCHAR(64),
    original_name VARCHAR(255),
    owner_id BIGINT
);
