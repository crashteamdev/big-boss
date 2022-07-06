--liquibase formatted sql
--changeset vitaxa:create-initial-schema

CREATE TABLE product
(
    id          uuid PRIMARY KEY,
    name        CHARACTER VARYING NOT NULL UNIQUE,
    description CHARACTER VARYING NOT NULL
);

CREATE TABLE subscription
(
    id          uuid PRIMARY KEY,
    product_id  uuid              NOT NULL,
    name        CHARACTER VARYING NOT NULL,
    description CHARACTER VARYING NOT NULL,
    price       BIGINT            NOT NULL,
    level       SMALLINT          NOT NULL,

    CONSTRAINT fk_subscription_product FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE UNIQUE INDEX subscription_product_id_level_idx on subscription (product_id, level);

CREATE UNIQUE INDEX subscription_name_idx on subscription (name);

CREATE TABLE account
(
    id      BIGSERIAL PRIMARY KEY,
    user_id CHARACTER VARYING NOT NULL,
    email   CHARACTER VARYING NOT NULL,
    blocked BOOLEAN DEFAULT FALSE
);

CREATE UNIQUE INDEX user_user_id_idx on account (user_id);

CREATE TABLE account_wallet
(
    wallet_id  CHARACTER VARYING PRIMARY KEY,
    account_id BIGINT NOT NULL,
    balance    BIGINT NOT NULL DEFAULT 0,
    blocked    BOOLEAN         DEFAULT FALSE,

    CONSTRAINT fk_account_wallet_account FOREIGN KEY (account_id) REFERENCES account (id)
);

CREATE UNIQUE INDEX account_wallet_account_id_idx on account_wallet (account_id);

CREATE TYPE subscription_state as ENUM (
    'active',
    'suspended'
    );

CREATE TABLE account_subscription
(
    id              BIGSERIAL PRIMARY KEY,
    account_id      BIGINT NOT NULL,
    subscription_id uuid   NOT NULL,
    valid_until     TIMESTAMP WITHOUT TIME ZONE,
    state           subscription_state,

    CONSTRAINT fk_account_subscription_account FOREIGN KEY (account_id) REFERENCES account (id),
    CONSTRAINT fk_account_subscription_subscription FOREIGN KEY (subscription_id) REFERENCES subscription (id)
);

CREATE UNIQUE INDEX account_subscription_account_id_idx on account_subscription (account_id);

CREATE TYPE saga_state as ENUM (
    'commit',
    'rollback',
    'in_progress'
    );

CREATE TABLE subscription_saga_coordinator
(
    trx_id          CHARACTER VARYING NOT NULL PRIMARY KEY,
    account_id      BIGINT            NOT NULL,
    state           saga_state        NOT NULL,
    subscription_id uuid              NOT NULL,

    CONSTRAINT fk_subscription_saga_coordinator_account FOREIGN KEY (account_id) REFERENCES account (id),
    CONSTRAINT fk_subscription_saga_coordinator_subscription FOREIGN KEY (subscription_id) REFERENCES subscription (id)
);

