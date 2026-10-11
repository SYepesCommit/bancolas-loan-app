
CREATE TABLE IF NOT EXISTS customer (
    id VARCHAR(50) PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    available_capacity DECIMAL(15, 2) NOT NULL,
    version BIGINT DEFAULT 0
);


CREATE TABLE IF NOT EXISTS loan_request (
    id BIGSERIAL PRIMARY KEY,
    request_reference VARCHAR(50) NOT NULL UNIQUE,
    customer_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    term_months INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    rejection_reason VARCHAR(255),
    processed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
);


INSERT INTO customer (id, status, available_capacity, version) VALUES
    ('USR-10', 'ACTIVE', 1000000.00, 0),
    ('USR-20', 'ACTIVE', 2000000.00, 0),
    ('USR-30', 'BLOCKED', 800000.00, 0)
ON CONFLICT (id) DO NOTHING;