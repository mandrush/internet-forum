-- Database: forumdb

-- DROP DATABASE forumdb;

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

CREATE TABLE IF NOT EXISTS "ForumPost"
(
    topic character varying(80)  NOT NULL,
    content character varying(400) NOT NULL,
    nickname character varying(21) NOT NULL,
    email character varying(254),
    secret character varying(10) NOT NULL,
    create_ts timestamp(6) without time zone NOT NULL,
    post_id BIGSERIAL PRIMARY KEY,
    update_ts timestamp without time zone NOT NULL
);


ALTER TABLE "ForumPost"
    OWNER to postgres;


-- Table: public."ForumReply"

-- DROP TABLE public."ForumReply";

CREATE TABLE IF NOT EXISTS "ForumReply"
(
    content character varying(400) NOT NULL,
    nickname character varying(21) NOT NULL,
    email character varying(254),
    parent_id bigint NOT NULL,
    "timestamp" timestamp(4) without time zone NOT NULL,
    reply_id BIGSERIAL PRIMARY KEY,
    secret character varying(10) NOT NULL,
    CONSTRAINT post_fk FOREIGN KEY (parent_id)
        REFERENCES "ForumPost" (post_id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
        NOT VALID
);

ALTER TABLE "ForumReply"
    OWNER to postgres;