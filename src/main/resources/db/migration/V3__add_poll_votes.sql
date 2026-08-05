CREATE TABLE poll_votes (
    poll_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (poll_id, user_id),
    CONSTRAINT fk_poll_votes_poll
        FOREIGN KEY (poll_id) REFERENCES polls(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_poll_votes_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_poll_votes_poll_option
        FOREIGN KEY (poll_id, option_id)
        REFERENCES poll_options(poll_id, id)
        ON DELETE CASCADE
);

CREATE INDEX idx_poll_votes_poll_option
    ON poll_votes(poll_id, option_id);
