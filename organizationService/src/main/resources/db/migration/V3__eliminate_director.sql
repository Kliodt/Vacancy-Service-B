
ALTER TABLE organization
DROP COLUMN director;

ALTER TABLE organization
ADD COLUMN password VARCHAR(64);
