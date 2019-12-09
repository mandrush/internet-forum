-- Database: forumdb

-- DROP DATABASE forumdb;

CREATE DATABASE forumdb
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;



-- SCHEMA: public

-- DROP SCHEMA public ;

CREATE SCHEMA IF NOT EXISTS public
    AUTHORIZATION postgres;

COMMENT ON SCHEMA public
    IS 'standard public schema';

GRANT ALL ON SCHEMA public TO PUBLIC;

GRANT ALL ON SCHEMA public TO postgres;


-- Table: public."ForumPost"

-- DROP TABLE public."ForumPost";

CREATE TABLE public."ForumPost"
(
    topic character varying(80) COLLATE pg_catalog."default" NOT NULL,
    content character varying(400) COLLATE pg_catalog."default" NOT NULL,
    nickname character varying(21) COLLATE pg_catalog."default" NOT NULL,
    email character varying(254) COLLATE pg_catalog."default",
    secret character varying(10) COLLATE pg_catalog."default" NOT NULL,
    create_ts timestamp(6) without time zone NOT NULL,
    post_id BIGSERIAL PRIMARY KEY,
    update_ts timestamp without time zone NOT NULL
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public."ForumPost"
    OWNER to postgres;


-- Table: public."ForumReply"

-- DROP TABLE public."ForumReply";

CREATE TABLE public."ForumReply"
(
    content character varying(400) COLLATE pg_catalog."default" NOT NULL,
    nickname character varying(21) COLLATE pg_catalog."default" NOT NULL,
    email character varying(254) COLLATE pg_catalog."default",
    parent_id bigint NOT NULL,
    "timestamp" timestamp(4) without time zone NOT NULL,
    reply_id BIGSERIAL PRIMARY KEY,
    secret character varying(10) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT post_fk FOREIGN KEY (parent_id)
        REFERENCES public."ForumPost" (post_id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
        NOT VALID
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public."ForumReply"
    OWNER to postgres;