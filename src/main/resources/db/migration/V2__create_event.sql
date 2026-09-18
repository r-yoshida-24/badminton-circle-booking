CREATE TABLE events (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    place VARCHAR(200) NOT NULL,
    capacity INT NOT NULL,
    description VARCHAR(1000),
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT ck_events_capacity CHECK (capacity > 0),
    CONSTRAINT ck_events_time_range CHECK (start_time < end_time)
);
