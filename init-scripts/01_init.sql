-- init-scripts/01_init.sql
CREATE TABLE IF NOT EXISTS flyway_schema_history
(
    installed_rank INTEGER PRIMARY KEY,
    version        VARCHAR(50),
    description    VARCHAR(200),
    type           VARCHAR(20),
    script         VARCHAR(1000),
    checksum       INTEGER,
    installed_by   VARCHAR(100),
    installed_on   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    execution_time INTEGER,
    success        BOOLEAN
);