CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Stores every incoming event with enrichment data
CREATE TABLE IF NOT EXISTS events (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    risk_score DECIMAL(3,2),
    anomaly_flag BOOLEAN DEFAULT FALSE,
    recommended_action VARCHAR(255),
    routed_to VARCHAR(100),
    routing_rule_id UUID,
    status VARCHAR(50) DEFAULT 'received',
    received_at TIMESTAMP DEFAULT NOW(),
    processed_at TIMESTAMP
    );

-- Stores configurable routing rules
CREATE TABLE IF NOT EXISTS routing_rules (
                                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(100) NOT NULL,
    condition_field VARCHAR(100) NOT NULL,
    condition_operator VARCHAR(20) NOT NULL,
    condition_value VARCHAR(100) NOT NULL,
    handler VARCHAR(100) NOT NULL,
    priority INT DEFAULT 1,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
    );

-- Stores dead letter queue events
CREATE TABLE IF NOT EXISTS dead_letter_events (
                                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_event_id UUID,
    event_type VARCHAR(100),
    payload JSONB,
    error_message TEXT,
    retry_count INT DEFAULT 0,
    failed_at TIMESTAMP DEFAULT NOW()
    );

-- Seed some default routing rules
INSERT INTO routing_rules
(event_type, condition_field, condition_operator,
 condition_value, handler, priority)
VALUES
    ('payment_failed', 'risk_score', 'gt', '0.7',
     'alert_handler', 1),
    ('payment_failed', 'risk_score', 'lte', '0.7',
     'database_handler', 2),
    ('order_placed', 'risk_score', 'gt', '0.8',
     'alert_handler', 1),
    ('order_placed', 'risk_score', 'lte', '0.8',
     'database_handler', 2),
    ('user_signup', 'anomaly_flag', 'eq', 'false',
     'database_handler', 1),
    ('system_alert', 'risk_score', 'gt', '0.5',
     'alert_handler', 1);