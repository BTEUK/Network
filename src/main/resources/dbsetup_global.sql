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

CREATE TABLE IF NOT EXISTS points_info
(
    uuid        CHAR(36)        NOT NULL,
    type        ENUM('POINTS',
    'BUILDING_POINTS')          NOT NULL,
    date        DATE            NOT NULL,
    value       INT             NOT NULL,
    PRIMARY KEY(uuid,type,date)
);

CREATE TABLE IF NOT EXISTS online_users
(
    uuid        CHAR(36)        NOT NULL,
    join_time   BIGINT          NOT NULL,
    last_point  BIGINT          NOT NULL,
    server      VARCHAR(64)     NOT NULL,
    PRIMARY KEY(uuid)
);

CREATE TABLE IF NOT EXISTS server_switch
(
    uuid        CHAR(36)        NOT NULL,
    from        VARCHAR(64)     NOT NULL,
    to          VARCHAR(64)     NOT NULL,
    time        BIGINT          NOT NULL,
    PRIMARY KEY(uuid)
);