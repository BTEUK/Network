CREATE TABLE IF NOT EXISTS plot_data
(
	uuid        INT         AUTO_INCREMENT,
	status      ENUM('unclaimed',
	'claimed','submitted','reviewing',
	'completed','deleted')  NOT NULL,
	size        INT         NOT NULL,
	difficulty  INT         NOT NULL,
	location    VARCHAR(64) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS plot_members
(
    id          INT         NOT NULL,
    uuid        CHAR(36)    NOT NULL,
    is_owner    TINYINT(1)  NULL DEFAULT 0,
    last_enter  BIGINT      NOT NULL,
    PRIMARY KEY(id, uuid)
);

CREATE TABLE IF NOT EXISTS accept_data
(
    id          INT         NOT NULL,
    uuid        CHAR(36)    NOT NULL,
    reviewer    CHAR(36)    NOT NULL,
    book_id     INT         NULL DEFAULT 0,
    accuracy    INT         NOT NULL,
    quality     INT         NOT NULL,
    accept_time BIGINT      NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS deny_data
(
    id          INT         NOT NULL,
    uuid        CHAR(36)    NOT NULL,
    reviewer    CHAR(36)    NOT NULL,
    book_id     INT         NOT NULL,
    attempt     INT         NOT NULL,
    deny_time   BIGINT      NOT NULL,
    PRIMARY KEY(id, uuid, attempt)
);

CREATE TABLE IF NOT EXISTS book_data
(
    id          INT         NOT NULL,
    page        INT         NOT NULL,
    contents    TEXT        NOT NULL,
    PRIMARY KEY(id, page)
);

CREATE TABLE IF NOT EXISTS plot_corners
(
    id          INT         NOT NULL,
    corner      INT         NOT NULL,
    x           INT         NOT NULL,
    z           INT         NOT NULL,
    PRIMARY KEY(id,corner)
);

CREATE TABLE IF NOT EXISTS location_data
(
    name        VARCHAR(64) NOT NULL,
    server      VARCHAR(64) NOT NULL,
    coordMin    INT         NOT NULL,
    coordMax    INT         NOT NULL,
    xTransform  INT         NOT NULL,
    zTransform  INT         NOT NULL,
    PRIMARY KEY(name)
);

CREATE TABLE IF NOT EXISTS plot_invites
(
    id          INT         NOT NULL,
    owner       CHAR(36)    NOT NULL,
    uuid        CHAR(36)    NOT NULL,
    PRIMARY KEY(id,uuid)
);

CREATE TABLE IF NOT EXISTS regions
(
    region      VARCHAR(16) NOT NULL,
    server      VARCHAR(64) NOT NULL,
    location    VARCHAR(64) NOT NULL,
    PRIMARY KEY(region)
);