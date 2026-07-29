CREATE TABLE api_call_history (
    id BIGSERIAL PRIMARY KEY,
    http_method VARCHAR(10) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    parameters JSONB,
    http_status_code INTEGER NOT NULL,
    response_body JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
