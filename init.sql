-- =====================================================
-- Insurance Claim System - PostgreSQL Schema
-- For Render Deployment
-- =====================================================

-- ROLES TABLE
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- POLICIES TABLE
CREATE TABLE IF NOT EXISTS policies (
    policy_number VARCHAR(50) PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES users(id),
    customer_name VARCHAR(255) NOT NULL,
    policy_type VARCHAR(50) NOT NULL,
    policy_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    coverage DECIMAL(15,2) NOT NULL,
    premium DECIMAL(10,2) NOT NULL,
    start_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CLAIMS TABLE
CREATE TABLE IF NOT EXISTS claims (
    id BIGSERIAL PRIMARY KEY,
    claim_number VARCHAR(50) NOT NULL UNIQUE,
    policy_number VARCHAR(50) NOT NULL REFERENCES policies(policy_number),
    claim_type VARCHAR(50) NOT NULL,
    incident_date DATE NOT NULL,
    description TEXT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    submitted_by BIGINT NOT NULL REFERENCES users(id),
    reviewed_by BIGINT REFERENCES users(id),
    review_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CLAIM ATTACHMENTS TABLE
CREATE TABLE IF NOT EXISTS claim_attachments (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- NOTIFICATIONS TABLE
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    claim_id BIGINT REFERENCES claims(id),
    notification_type VARCHAR(50) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- INDEXES
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_policies_customer_id ON policies(customer_id);
CREATE INDEX IF NOT EXISTS idx_policies_status ON policies(policy_status);
CREATE INDEX IF NOT EXISTS idx_claims_policy_number ON claims(policy_number);
CREATE INDEX IF NOT EXISTS idx_claims_status ON claims(status);
CREATE INDEX IF NOT EXISTS idx_claims_submitted_by ON claims(submitted_by);

-- SAMPLE DATA - ROLES
INSERT INTO roles (name, description) VALUES
('CUSTOMER', 'Insurance customer'),
('ADJUSTER', 'Insurance adjuster'),
('ADMIN', 'System administrator')
ON CONFLICT (name) DO NOTHING;

-- SAMPLE DATA - USERS (password: password123 - BCrypt)
INSERT INTO users (username, password, email, full_name, phone, role_id) VALUES
('john.doe', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rCQJxaRFTtMFj/HbZe', 'john.doe@email.com', 'John Doe', '+1-555-0101', 1),
('jane.smith', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rCQJxaRFTtMFj/HbZe', 'jane.smith@email.com', 'Jane Smith', '+1-555-0102', 1),
('mike.johnson', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rCQJxaRFTtMFj/HbZe', 'mike.johnson@insurance.com', 'Mike Johnson', '+1-555-0201', 2),
('sarah.wilson', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rCQJxaRFTtMFj/HbZe', 'sarah.wilson@insurance.com', 'Sarah Wilson', '+1-555-0202', 2),
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rCQJxaRFTtMFj/HbZe', 'admin@insurance.com', 'System Admin', '+1-555-0301', 3)
ON CONFLICT (username) DO NOTHING;

-- SAMPLE DATA - POLICIES
INSERT INTO policies (policy_number, customer_id, customer_name, policy_type, policy_status, coverage, premium, start_date, expiry_date) VALUES
('POL-001', 1, 'John Doe', 'HEALTH', 'ACTIVE', 100000.00, 500.00, '2024-01-01', '2025-12-31'),
('POL-002', 1, 'John Doe', 'AUTO', 'ACTIVE', 50000.00, 300.00, '2024-03-15', '2025-03-14'),
('POL-003', 2, 'Jane Smith', 'HOME', 'ACTIVE', 250000.00, 800.00, '2024-02-01', '2026-01-31')
ON CONFLICT (policy_number) DO NOTHING;

-- SAMPLE DATA - CLAIMS
INSERT INTO claims (claim_number, policy_number, claim_type, incident_date, description, amount, status, submitted_by, reviewed_by, review_notes) VALUES
('CLM-2024-001', 'POL-001', 'HEALTH', '2024-06-15', 'Hospitalization due to accident', 15000.00, 'APPROVED', 1, 3, 'Verified with hospital records. Approved.'),
('CLM-2024-002', 'POL-002', 'AUTO', '2024-07-20', 'Car accident at intersection', 8500.00, 'IN_REVIEW', 1, 4, 'Awaiting police report.')
ON CONFLICT (claim_number) DO NOTHING;
