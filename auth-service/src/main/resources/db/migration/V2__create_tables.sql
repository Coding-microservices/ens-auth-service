CREATE TABLE IF NOT EXISTS roles
(
    id   BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts
(
    id                    BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    account_id            UUID        NOT NULL,
    email                 TEXT        NOT NULL,
    password_hash         TEXT,
    is_soft_deleted       BOOLEAN     NOT NULL DEFAULT FALSE,
    blocked_until         TIMESTAMP,
    first_name            VARCHAR(50),
    last_name             VARCHAR(50),
    phone_number          VARCHAR(15),
    created_at            TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    role_id               BIGINT      NOT NULL,
    CONSTRAINT fk_account_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE IF NOT EXISTS temporary_passwords
(
    id                      BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    account_id              BIGINT NOT NULL UNIQUE,
    temporary_password_hash TEXT,
    expiration_date         TIMESTAMP,
    CONSTRAINT fk_temp_password_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    token       TEXT,
    expiry_date TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    account_id  BIGINT    NOT NULL UNIQUE,
    CONSTRAINT fk_refresh_token_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS admins
(
    id             BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    account_id     BIGINT  NOT NULL UNIQUE,
    is_super_admin BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_admin_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS users
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    account_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_client_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE
);
