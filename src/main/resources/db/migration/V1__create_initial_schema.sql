-- Начальная схема базы данных
-- Создание основных таблиц: users, subscriptions, payments

-- Таблица пользователей
CREATE TABLE users (
                       id bigint PRIMARY KEY,
                       telegram_id BIGINT UNIQUE NOT NULL,
                       username VARCHAR(255),
                       first_name VARCHAR(255),
                       last_name VARCHAR(255),
                       registration_date TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Индекс для быстрого поиска по Telegram ID
CREATE INDEX idx_users_telegram_id ON users(telegram_id);

-- Таблица подписок
CREATE TABLE subscriptions (
                               id bigint PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               tariff_plan VARCHAR(20) NOT NULL,
                               start_date TIMESTAMP NOT NULL,
                               end_date TIMESTAMP NOT NULL,
                               remaining_requests INTEGER NOT NULL,
                               daily_limit INTEGER NOT NULL,
                               CONSTRAINT fk_subscription_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Индексы для подписок
CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_end_date ON subscriptions(end_date);

-- Таблица платежей
CREATE TABLE payments (
                          id BIGINT PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          payment_id VARCHAR(255) UNIQUE,
                          amount DECIMAL(10, 2) NOT NULL,
                          currency VARCHAR(3) NOT NULL,
                          status VARCHAR(20) NOT NULL,
                          tariff_plan VARCHAR(20) NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMP,
                          CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Индексы для платежей
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_payment_id ON payments(payment_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at);

-- Таблица настроек системы
CREATE TABLE system_settings (
                                 id bigint PRIMARY KEY,
                                 setting_key VARCHAR(100) UNIQUE NOT NULL,
                                 setting_value TEXT,
                                 description TEXT,
                                 updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Вставка начальных настроек
INSERT INTO system_settings (setting_key, setting_value, description)
VALUES
    ('tariff.romantic.price', '990', 'Цена тарифа "Романтик" в рублях'),
    ('tariff.romantic.daily_limit', '50', 'Дневной лимит запросов для тарифа "Романтик"'),
    ('tariff.alpha.price', '1990', 'Цена тарифа "Альфач" в рублях'),
    ('tariff.alpha.daily_limit', '150', 'Дневной лимит запросов для тарифа "Альфач"'),
    ('tariff.lovelace.price', '4990', 'Цена тарифа "Ловелас" в рублях'),
    ('tariff.lovelace.daily_limit', '9999', 'Дневной лимит запросов для тарифа "Ловелас" (безлимит)'),
    ('api.coze.retry_count', '3', 'Количество повторных попыток при сбое Coze API'),
    ('api.coze.retry_delay_ms', '2000', 'Задержка между повторными попытками в миллисекундах'),
    ('bot.welcome_message', 'Добро пожаловать в CozeTalk! 👋\n\nЯ готов помочь вам с вашими запросами, используя нейромодель Coze API.\n\n🔹 Отправьте мне любой вопрос или запрос.\n🔹 Используйте /tariff для выбора тарифного плана.\n🔹 Используйте /profile для просмотра информации о вашем профиле.\n🔹 Используйте /help чтобы увидеть все доступные команды.', 'Приветственное сообщение бота');

-- Создание таблицы администраторов
CREATE TABLE admins (
                        id bigint PRIMARY KEY,
                        username VARCHAR(255) UNIQUE NOT NULL,
                        password_hash VARCHAR(255) NOT NULL,
                        email VARCHAR(255) UNIQUE NOT NULL,
                        first_name VARCHAR(255),
                        last_name VARCHAR(255),
                        role VARCHAR(50) NOT NULL DEFAULT 'ADMIN',
                        created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                        last_login TIMESTAMP
);

-- Создание тестового администратора (пароль: admin)
-- В продакшене следует заменить на реальные данные
INSERT INTO admins (username, password_hash, email, first_name, last_name, role)
VALUES ('admin', '$2a$12$jsTVqLPSt7pQG4rKlMmX7.HHjlJTYgkCGqTiIy0Kxw7zhP7zvHmFm', 'admin@example.com', 'Admin', 'User', 'SUPER_ADMIN');