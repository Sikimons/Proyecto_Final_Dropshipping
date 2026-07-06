CREATE TABLE IF NOT EXISTS dropshipping_order
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_code           VARCHAR(50)  NOT NULL UNIQUE,
    provider_id          BIGINT       NOT NULL,
    product_code         VARCHAR(100) NOT NULL,
    product_description  VARCHAR(500) NOT NULL,
    quantity             INT          NOT NULL CHECK (quantity > 0),
    street               VARCHAR(255) NOT NULL,
    city                 VARCHAR(100) NOT NULL,
    state                VARCHAR(100) NOT NULL,
    postal_code          VARCHAR(20),
    country              VARCHAR(100) NOT NULL,
    customer_name        VARCHAR(200) NOT NULL,
    customer_contact     VARCHAR(200) NOT NULL,
    expected_delivery_date DATE       NOT NULL,
    special_conditions   TEXT,
    status               VARCHAR(20)  NOT NULL,
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS order_status_event
(
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id               BIGINT       NOT NULL,
    previous_status        VARCHAR(20)  NOT NULL,
    new_status             VARCHAR(20)  NOT NULL,
    actor_id               VARCHAR(100) NOT NULL,
    timestamp              TIMESTAMP    NOT NULL,
    estimated_dispatch_date DATE,
    rejection_reason       TEXT,
    CONSTRAINT fk_event_order FOREIGN KEY (order_id) REFERENCES dropshipping_order (id)
);
