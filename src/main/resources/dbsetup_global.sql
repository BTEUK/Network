CREATE TABLE IF NOT EXISTS player_data
(
	uuid		CHAR(36)		NOT NULL,
	name		VARCHAR(16)		NOT NULL,
	last_online	BIGINT			NOT NULL,
	last_submit	BIGINT			NOT NULL,
	navigator   TINYINT(1)      NOT NULL DEFAULT 1,
	teleport_enabled    TINYINT(1)  NOT NULL DEFAULT 1,
	nightvision_enabled TINYINT(1)  NOT NULL DEFAULT 0,
	previous_coordinate INT     NOT NULL DEFAULT 0,
	PRIMARY KEY (uuid)
);

CREATE TABLE IF NOT EXISTS messages
(
    recipient   CHAR(36)        NOT NULL,
    message     VARCHAR(256)    NOT NULL,
    PRIMARY KEY(recipient,message)
);

CREATE TABLE IF NOT EXISTS join_events
(
    uuid        CHAR(36)        NOT NULL,
    type        ENUM('plotsystem',
                'network')      NOT NULL,
    event       VARCHAR(256)    NOT NULL,
    PRIMARY KEY(uuid)
);

CREATE TABLE IF NOT EXISTS server_events
(
    uuid        CHAR(36)        NULL DEFAULT NULL,
    type        ENUM('plotsystem',
                'network')      NOT NULL,
    server      VARCHAR(64)     NOT NULL,
    event       VARCHAR(256)    NOT NULL,
    UNIQUE(uuid,event)
);

CREATE TABLE IF NOT EXISTS points_data
(
    uuid        CHAR(36)        NOT NULL,
    points      INT             NOT NULL,
    points_weekly   INT         NOT NULL,
    building_points INT         NOT NULL,
    building_points_monthly INT NOT NULL,
    messages    INT             NOT NULL,
    online_time INT             NOT NULL,
    PRIMARY KEY(uuid)
);

CREATE TABLE IF NOT EXISTS points_info
(
    uuid        CHAR(36)        NOT NULL,
    type        ENUM('POINTS',
    'BUILDING_POINTS')          NOT NULL,
    on_date        DATE            NOT NULL,
    points       INT             NOT NULL,
    PRIMARY KEY(uuid,type,on_date)
);

CREATE TABLE IF NOT EXISTS online_users
(
    uuid        CHAR(36)        NOT NULL,
    join_time   BIGINT          NOT NULL,
    last_ping   BIGINT          NOT NULL,
    server      VARCHAR(64)     NOT NULL,
    PRIMARY KEY(uuid)
);

CREATE TABLE IF NOT EXISTS server_switch
(
    uuid        CHAR(36)        NOT NULL,
    from_server VARCHAR(64)     NOT NULL,
    to_server   VARCHAR(64)     NOT NULL,
    switch_time BIGINT          NOT NULL,
    PRIMARY KEY(uuid)
);

CREATE TABLE IF NOT EXISTS coordinates
(
    id          INT         AUTO_INCREMENT,
    server      VARCHAR(64) NOT NULL,
    world       VARCHAR(64) NOT NULL,
    x           DOUBLE      NOT NULL,
    y           DOUBLE      NULL DEFAULT 0.0,
    z           DOUBLE      NOT NULL,
    yaw         FLOAT       NULL DEFAULT 0.0,
    pitch       FLOAT       NULL DEFAULT 0.0,
    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS location_data
(
    location    VARCHAR(128)    NOT NULL,
    category    VARCHAR(128)    NOT NULL,
    subcategory VARCHAR(128)    NOT NULL,
    coordinate  INT             NOT NULL,
    PRIMARY KEY(location)
);

CREATE TABLE IF NOT EXISTS location_requests
(
    location    VARCHAR(128)    NOT NULL,
    coordinate  INT             NOT NULL,
    PRIMARY KEY(location)
);

CREATE TABLE IF NOT EXISTS server_data
(
    name          VARCHAR(64)   NOT NULL,
    type        ENUM('PLOT','EARTH',
    'LOBBY','TUTORIAL')     NOT NULL,
    PRIMARY KEY(name)
);