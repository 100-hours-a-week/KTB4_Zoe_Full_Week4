CREATE TABLE post_draft_poll_options (
    draft_id BIGINT NOT NULL,
    option_order INTEGER NOT NULL,
    content VARCHAR(30) NOT NULL,
    CONSTRAINT fk_post_draft_poll_options_draft
        FOREIGN KEY (draft_id) REFERENCES post_drafts(id) ON DELETE CASCADE,
    CONSTRAINT uk_post_draft_poll_options_order UNIQUE (draft_id, option_order),
    CONSTRAINT chk_post_draft_poll_options_content
        CHECK (CHAR_LENGTH(TRIM(content)) BETWEEN 1 AND 30),
    CONSTRAINT chk_post_draft_poll_options_order CHECK (option_order >= 0)
);
