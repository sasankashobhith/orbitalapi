DROP TABLE IF EXISTS test_user CASCADE;

CREATE TABLE IF NOT EXISTS test_user
(
	userid serial primary key,
    name text not null
);