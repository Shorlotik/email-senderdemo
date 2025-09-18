# 📧 Email Sender Demo

Современное Spring Boot приложение для массовой рассылки email с функцией отписки.

## ✨ Возможности

- 📨 **Массовая рассылка** - отправка email большому количеству получателей
- 🗄️ **База данных** - хранение подписчиков и логов рассылок в PostgreSQL
- 🚫 **Отписка** - возможность отписаться от рассылки через веб-интерфейс
- ⏱️ **Контроль скорости** - настраиваемая задержка между отправками
- 🎨 **HTML шаблоны** - красивые HTML письма с адаптивным дизайном
- 📊 **Логирование** - отслеживание отправленных писем по кампаниям

## 🛠️ Технологии

- **Java 17**
- **Spring Boot 3.5.0**
- **PostgreSQL**
- **Thymeleaf** (для веб-страниц)
- **Jakarta Mail** (для отправки email)

## 🚀 Быстрый старт

### 1. Клонирование проекта

```bash
git clone <your-repository-url>
cd email-senderdemo
```

### 2. Настройка базы данных

Создайте базу данных PostgreSQL и выполните следующие SQL команды:

```sql
-- Создание таблицы подписчиков
CREATE TABLE email_subscribers (
    email_address VARCHAR(255) PRIMARY KEY,
    is_subscribed BOOLEAN DEFAULT TRUE,
    mailbox_exists BOOLEAN DEFAULT TRUE
);

-- Создание таблицы логов кампаний
CREATE TABLE email_campaign_logs (
    id SERIAL PRIMARY KEY,
    email_address VARCHAR(255),
    campaign_id VARCHAR(100),
    sent_at TIMESTAMP DEFAULT NOW()
);

-- Добавление тестовых данных
INSERT INTO email_subscribers (email_address, is_subscribed, mailbox_exists)
VALUES
('test1@example.com', TRUE, TRUE),
('test2@example.com', TRUE, TRUE),
('test3@example.com', TRUE, TRUE);
```

### 3. Настройка переменных окружения

1. Скопируйте файл `.env.example` в `.env`:
   ```bash
   cp .env.example .env
   ```

2. Отредактируйте файл `.env` и укажите ваши настройки:
   ```env
   # Database Configuration
   DB_URL=jdbc:postgresql://localhost:5432/emaildb
   DB_USERNAME=postgres
   DB_PASSWORD=your_database_password_here

   # Email Configuration
   SMTP_HOST=smtp.gmail.com
   SMTP_PORT=587
   SMTP_USERNAME=your_email@gmail.com
   SMTP_PASSWORD=your_app_password_here

   # Email Campaign Settings
   EMAIL_CAMPAIGN_DELAY_MS=300
   EMAIL_CAMPAIGN_DEFAULT_SUBJECT=Добро пожаловать в нашу рассылку!

   # Application Settings
   SERVER_PORT=8080
   ```

**Важно**: Файл `.env` не попадает в git репозиторий для безопасности ваших данных.

### 4. Запуск приложения

```bash
./mvnw spring-boot:run
```

Приложение будет доступно по адресу: `http://localhost:8080`

## 📋 Использование

### Отправка рассылки

1. Настройте параметры кампании в методе `sendCampaignEmails()`:
   ```java
   String campaignId = "your_campaign_id";
   String subject = "Тема вашего письма";
   String htmlFilePath = "src/main/resources/email_template.html";
   ```

2. Запустите приложение - рассылка начнется автоматически

### Отписка от рассылки

Пользователи могут отписаться, перейдя по ссылке:
```
http://localhost:8080/api/unsubscribe?email=user@example.com
```

## 📁 Структура проекта

```
src/
├── main/
│   ├── java/com/example/emailsenderdemo/
│   │   ├── EmailSenderdemoApplication.java    # Основной класс приложения
│   │   ├── UnsubscribeController.java         # API для отписки
│   │   └── UnsubscribePageController.java     # Контроллер страниц
│   └── resources/
│       ├── application.properties             # Конфигурация
│       ├── email_template.html               # HTML шаблон письма
│       └── templates/
│           └── unsubscribed.html             # Страница подтверждения отписки
└── test/                                     # Тесты
```

## ⚙️ Настройки

### Конфигурация email

- **Gmail**: Используйте пароль приложения вместо обычного пароля
- **Другие провайдеры**: Обновите настройки SMTP в `application.properties`

### Контроль скорости отправки

По умолчанию установлена задержка 300мс между отправками (200 писем в минуту). 
Измените значение `DELAY_MS_BETWEEN_EMAILS` для настройки скорости.

## 🔒 Безопасность

- ✅ Все пароли и чувствительные данные хранятся в переменных окружения (`.env` файл)
- ✅ Файл `.env` исключен из git репозитория
- ✅ Используйте HTTPS в продакшене
- ✅ Настройте правильные CORS политики
- ✅ Для Gmail используйте пароль приложения вместо обычного пароля

## 📝 Лицензия

Этот проект создан в образовательных целях.

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте ветку для новой функции
3. Внесите изменения
4. Создайте Pull Request

---

**Примечание**: Убедитесь, что у вас есть права на отправку email через выбранный SMTP сервер и соблюдаете правила антиспама.
