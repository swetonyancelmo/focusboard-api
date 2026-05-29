CREATE TABLE refresh_tokens (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL,
    token       TEXT        NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_token UNIQUE (token),
    CONSTRAINT uq_refresh_tokens_user  UNIQUE (user_id),
    CONSTRAINT fk_refresh_tokens_user  FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);