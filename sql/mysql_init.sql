CREATE DATABASE `MOON` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

use MOON;
/*创建用，设置密码*/
create user 'moonlight'@'%' identified by '1NN7Eax4BeEH4UL';
/*授予用户权限*/
grant all privileges on *.* to 'moonlight'@'%' with grant option;

CREATE TABLE T_USER
(
    ID          varchar(32)  NOT NULL,
    USERNAME    varchar(200) not NULL,
    PASSWORD    varchar(10)  not NULL,
    MOBILE      varchar(2048),
    EMAIL       varchar(1024),
    CREATE_TIME datetime              default now(),
    UPDATE_TIME datetime              default now(),
    CREATOR_ID  varchar(32),
    MODIFIER_ID varchar(32),
    DELETED     int          not null default 1 comment '逻辑删除标识，1：存在，0：被删除，默认为1',
    STATUS      int,
    PRIMARY KEY (ID),
    INDEX (ID),
    UNIQUE (USERNAME)
) COMMENT ='用户表' ENGINE = InnoDB
                 DEFAULT CHARSET = utf8mb4;


CREATE TABLE T_ROLE
(
    ID          varchar(32)  NOT NULL,
    NAME        VARCHAR(128) not null,
    CODE        VARCHAR(128) not null,
    PARENT_ID   varchar(32)  not null,
    CREATE_TIME datetime              default now(),
    UPDATE_TIME datetime              default now(),
    CREATOR_ID  varchar(32),
    MODIFIER_ID varchar(32),
    DELETED     int          not null default 1 comment '逻辑删除标识，1：存在，0：被删除，默认为1',
    PRIMARY KEY (ID),
    INDEX (ID)
) COMMENT ='角色表' ENGINE = InnoDB
                 DEFAULT CHARSET = utf8mb4;


CREATE TABLE T_PERMISSION
(
    ID          varchar(32)  NOT NULL,
    CODE        VARCHAR(128) not null,
    NAME        VARCHAR(128) not null,
    CREATE_TIME datetime              default now(),
    UPDATE_TIME datetime              default now(),
    CREATOR_ID  varchar(32),
    MODIFIER_ID varchar(32),
    DELETED     int          not null default 1 comment '逻辑删除标识，1：存在，0：被删除，默认为1',
    PRIMARY KEY (ID),
    INDEX (ID)
) COMMENT ='权限表' ENGINE = InnoDB
                 DEFAULT CHARSET = utf8mb4;


CREATE TABLE T_USER_ROLE
(
    ID          varchar(32) NOT NULL,
    USER_ID     VARCHAR(32) not null,
    ROLE_ID     varchar(32) not null,
    CREATE_TIME datetime             default now(),
    UPDATE_TIME datetime             default now(),
    CREATOR_ID  varchar(32),
    MODIFIER_ID varchar(32),
    DELETED     int         not null default 1 comment '逻辑删除标识，1：存在，0：被删除，默认为1',
    PRIMARY KEY (ID),
    INDEX (ID)
) COMMENT ='用户角色关联表' ENGINE = InnoDB
                     DEFAULT CHARSET = utf8mb4;

CREATE TABLE T_ROLE_PERMISSION
(
    ID            varchar(32) NOT NULL,
    ROLE_ID       VARCHAR(32) not null,
    PERMISSION_ID varchar(32) not null,
    CREATE_TIME   datetime             default now(),
    UPDATE_TIME   datetime             default now(),
    CREATOR_ID    varchar(32),
    MODIFIER_ID   varchar(32),
    PRIMARY KEY (ID),
    DELETED       int         not null default 1 comment '逻辑删除标识，1：存在，0：被删除，默认为1',
    INDEX (ID)
) COMMENT ='角色权限关联表' ENGINE = InnoDB
                     DEFAULT CHARSET = utf8mb4;

CREATE TABLE T_ACTION
(
    ID      VARCHAR(32)  NOT NULL,
    NAME    VARCHAR(128) NOT NULL,
    CODE    VARCHAR(128) NOT NULL,
    PRIMARY KEY (ID),
    DELETED int          not null default 1 comment '逻辑删除标识，1：存在，0：被删除，默认为1'

) COMMENT ='动作表' ENGINE = InnoDB
                 DEFAULT CHARSET = utf8mb4;


CREATE TABLE T_MENU
(
    ID          VARCHAR(32)  NOT NULL,
    NAME        VARCHAR(128) NOT NULL,
    CODE        VARCHAR(128) NOT NULL,
    MODULE      VARCHAR(128) NOT NULL,
    PARENT_ID   VARCHAR(32)  NOT NULL,
    CREATE_TIME datetime              default now(),
    UPDATE_TIME datetime              default now(),
    CREATOR_ID  varchar(32),
    MODIFIER_ID varchar(32),
    PRIMARY KEY (ID),
    DELETED     int          not null default 1 comment '逻辑删除标识，1：存在，0：被删除，默认为1',
    INDEX (ID)

) COMMENT ='菜单表' ENGINE = InnoDB
                 DEFAULT CHARSET = utf8mb4;