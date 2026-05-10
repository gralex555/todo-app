--liquibase formatted sql

--changeset alex:create-audit-log
--comment: Таблица для асинхронного аудита действий пользователей
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details VARCHAR(500)
);
--rollback DROP TABLE audit_log;

--changeset alex:create-audit-log-indexes
--comment: Индексы для быстрых запросов по пользователю и времени
CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);
--rollback DROP INDEX idx_audit_log_user_id;
--rollback DROP INDEX idx_audit_log_created_at;
