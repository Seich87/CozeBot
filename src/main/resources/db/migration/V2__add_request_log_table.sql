-- Добавление таблицы логов запросов
-- Эта таблица хранит информацию о всех запросах пользователей к Coze API

-- Таблица логов запросов
CREATE TABLE request_logs (
                              id BIGINT PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              request_time TIMESTAMP NOT NULL DEFAULT NOW(),
                              request_text TEXT NOT NULL,
                              response_text TEXT,
                              status VARCHAR(20) NOT NULL,
                              process_time INTEGER,
                              CONSTRAINT fk_request_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Индексы для быстрого поиска и фильтрации логов
CREATE INDEX idx_request_logs_user_id ON request_logs(user_id);
CREATE INDEX idx_request_logs_request_time ON request_logs(request_time);
CREATE INDEX idx_request_logs_status ON request_logs(status);

-- Таблица для аналитики использования
CREATE TABLE usage_statistics (
                                  id BIGSERIAL PRIMARY KEY,
                                  date DATE NOT NULL,
                                  total_requests INTEGER NOT NULL DEFAULT 0,
                                  unique_users INTEGER NOT NULL DEFAULT 0,
                                  avg_process_time INTEGER,
                                  error_count INTEGER NOT NULL DEFAULT 0,
                                  CONSTRAINT usage_statistics_date_unique UNIQUE (date)
);

-- Триггерная функция для обновления статистики использования
CREATE OR REPLACE FUNCTION update_usage_statistics()
RETURNS TRIGGER AS $$
DECLARE
    curr_date DATE;
unique_users_count INTEGER;
avg_process_time_value INTEGER;
BEGIN
    curr_date := CURRENT_DATE;

-- Проверка статуса запроса для учета ошибок
IF NEW.status = 'ERROR' THEN
        -- Увеличиваем счетчик ошибок
UPDATE usage_statistics
SET error_count = error_count + 1
WHERE date = curr_date;
END IF;

    -- Подсчет уникальных пользователей за текущий день
SELECT COUNT(DISTINCT user_id) INTO unique_users_count
FROM request_logs
WHERE DATE(request_time) = curr_date;

-- Расчет среднего времени обработки
SELECT AVG(process_time) INTO avg_process_time_value
FROM request_logs
WHERE DATE(request_time) = curr_date
  AND process_time IS NOT NULL;

-- Вставка или обновление записи статистики
INSERT INTO usage_statistics (date, total_requests, unique_users, avg_process_time)
VALUES (curr_date, 1, unique_users_count, avg_process_time_value)
ON CONFLICT (date)
DO UPDATE SET
    total_requests = usage_statistics.total_requests + 1,
    unique_users = unique_users_count,
    avg_process_time = avg_process_time_value;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Создание триггера для автоматического обновления статистики
CREATE TRIGGER request_log_insert_trigger
    AFTER INSERT ON request_logs
    FOR EACH ROW
    EXECUTE FUNCTION update_usage_statistics();

-- Добавление системных настроек для логирования
INSERT INTO system_settings (setting_key, setting_value, description)
VALUES
    ('logging.request.enabled', 'true', 'Включение/отключение логирования запросов'),
    ('logging.request.retention_days', '90', 'Количество дней хранения логов запросов'),
    ('logging.admin_notification.critical_errors', 'true', 'Включение/отключение уведомлений администратора о критических ошибках');

-- Представление для анализа пользовательской активности
CREATE VIEW user_activity_view AS
SELECT
    u.id AS user_id,
    u.telegram_id,
    u.username,
    COUNT(r.id) AS total_requests,
    MAX(r.request_time) AS last_activity,
    COUNT(CASE WHEN r.status = 'ERROR' THEN 1 END) AS error_count,
    AVG(r.process_time) AS avg_process_time
FROM
    users u
        LEFT JOIN
    request_logs r ON u.id = r.user_id
GROUP BY
    u.id, u.telegram_id, u.username;

-- Представление для анализа популярных запросов
CREATE VIEW popular_requests_view AS
SELECT
    request_text,
    COUNT(*) AS request_count,
    AVG(process_time) AS avg_process_time
FROM
    request_logs
WHERE
        request_time > CURRENT_DATE - INTERVAL '30 days'
GROUP BY
    request_text
HAVING
        COUNT(*) > 2
ORDER BY
    request_count DESC;