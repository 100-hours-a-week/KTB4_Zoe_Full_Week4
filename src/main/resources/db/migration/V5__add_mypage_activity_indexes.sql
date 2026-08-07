CREATE INDEX idx_posts_user_status_created_id
    ON posts(user_id, status, created_at, id);

CREATE INDEX idx_post_likes_user_created_post
    ON post_likes(user_id, created_at, post_id);

CREATE INDEX idx_poll_votes_user_updated_poll
    ON poll_votes(user_id, updated_at, poll_id);
