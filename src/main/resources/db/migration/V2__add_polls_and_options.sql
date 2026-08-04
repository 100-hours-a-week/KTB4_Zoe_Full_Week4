CREATE TABLE polls (
    post_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (post_id),
    CONSTRAINT fk_polls_post
        FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

CREATE TABLE poll_options (
    id BIGINT AUTO_INCREMENT NOT NULL,
    poll_id BIGINT NOT NULL,
    content VARCHAR(30) NOT NULL,
    option_order INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_poll_options_poll
        FOREIGN KEY (poll_id) REFERENCES polls(post_id) ON DELETE CASCADE,
    CONSTRAINT uk_poll_options_poll_order UNIQUE (poll_id, option_order),
    CONSTRAINT uk_poll_options_poll_id UNIQUE (poll_id, id),
    CONSTRAINT chk_poll_options_content
        CHECK (CHAR_LENGTH(TRIM(content)) BETWEEN 1 AND 30),
    CONSTRAINT chk_poll_options_order CHECK (option_order >= 0)
);
