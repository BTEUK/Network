CREATE TABLE IF NOT EXISTS player_data
(
	uuid		CHAR(36)		NOT NULL,
	name		VARCHAR(16)		NOT NULL,
	last_join	BIGINT			NOT NULL,
	last_submit	BIGINT			NOT NULL,
	PRIMARY KEY (uuid)
);

CREATE TABLE IF NOT EXISTS messages
(
    id          INT             AUTO_INCREMENT,
    recipient   CHAR(36)        NOT NULL,
    messages    TEXT            NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS join_events
(
    uuid        CHAR(36)        NOT NULL,
    join_events TEXT            NOT NULL,
    PRIMARY KEY(uuid)
);

CREATE TABLE IF NOT EXISTS server_events
(
    uuid        CHAR(36)        NOT NULL,
    type        ENUM('plotsystem',
                'network')      NOT NULL,
    server      VARCHAR(64)     NOT NULL,
    event       TEXT            NOT NULL,
    PRIMARY KEY(uuid)
);

CREATE TABLE IF NOT EXISTS points_data
(
    uuid        CHAR(36)        NOT NULL,
    points      INT             NOT NULL,
    points_weekly   INT         NOT NULL,
    building_points INT         NOT NULL,
    building_points_monthly INT NOT NULL,
    messages    INT             NOT NULL,
    time        INT             NOT NULL,
    PRIMARY KEY(uuid)
);