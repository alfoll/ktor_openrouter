# Ktor AI Rewrite Service

Backend-сервис на **Ktor**, который принимает текст, отправляет его в **OpenRouter**, получает ответ модели и сохраняет результат в **SQLite**.

**Кратко**: сервис имеет 4 endpoint’а, работает через слой `route -> service -> внешний API -> DB`, использует **Koin**, **Exposed + SQLite**, валидирует пустой текст и содержит несколько простых тестов.

---

## Что делает сервис

Сервис поддерживает 4 endpoint’а:

- `POST /ai/rewrite` — принимает текст, отправляет его в OpenRouter, сохраняет `originalText`, `aiResponse`, `createdAt`
- `GET /ai/history` — возвращает всю историю запросов
- `GET /ai/history/{id}` — возвращает одну запись по id
- `DELETE /ai/history/{id}` — удаляет запись по id

---

## Стек

- **Kotlin**
- **Ktor**
- **Koin**
- **Exposed**
- **SQLite**
- **kotlinx.serialization**
- **StatusPages** для централизованной обработки ошибок
- **Java HttpClient** для вызова OpenRouter API

---

## Архитектура проекта

### 1. База данных
Таблица `ai_history` хранит:

- `id`
- `original_text`
- `ai_response`
- `created_at`

При старте приложения таблица создается автоматически через `SchemaUtils.create(AiHistory)`.

### 2. Repository layer
`AiRepository` отвечает за работу с SQLite:

- создание записи
- получение всех записей
- получение записи по `id`
- удаление записи по `id`

### 3. Service layer
`AiService` отвечает за бизнес-логику:

- проверка, что текст не пустой
- отправка запроса в OpenRouter
- разбор ответа
- сохранение результата в БД
- проверка существования записи по `id`

### 4. Routing layer
Маршруты принимают HTTP-запросы, вызывают сервис и возвращают HTTP-ответы.

### 5. DI
Все основные зависимости подключаются через **Koin**:

- `Database`
- `AiRepository`
- `AiService`

---

## Конфигурация

### `application.yaml`

```yaml
ktor:
  application:
    modules:
      - com.alfoll.ApplicationKt.module
  deployment:
    port: 8080

storage:
  url: ${STOR_URL}
  driver: ${STOR_DRIVER:org.sqlite.JDBC}

open_router:
  api_key: ${OPENROUTER_API_KEY}
  base_url: ${OPENROUTER_URL:https://openrouter.ai/api/v1/chat/completions}
  model: ${OPENROUTER_MODEL:openrouter/free}
```

### Переменные окружения

Нужно задать:

```env
STOR_URL=your_storage_url
STOR_DRIVER=org.sqlite.JDBC
OPENROUTER_API_KEY=your_openrouter_key
OPENROUTER_URL=https://openrouter.ai/api/v1/chat/completions
OPENROUTER_MODEL=openrouter/free
```
> Если `OPENROUTER_MODEL=openrouter/free` работает нестабильно, можно подставить конкретную бесплатную модель из OpenRouter.

---

## Запуск проекта

### 1. Клонировать репозиторий

```bash
git clone <URL_ВАШЕГО_РЕПОЗИТОРИЯ>
cd <ИМЯ_ПРОЕКТА>
```

### 2. Указать переменные окружения

Через терминал, IDE или `Run/Debug Configuration`.

### 3. Запустить приложение

```bash
./gradlew run
```

После запуска сервер будет доступен по адресу:

```text
http://localhost:8080
```

---

## API

### 1. POST `/ai/rewrite`

Принимает текст, отправляет его в OpenRouter и сохраняет результат в БД.

#### Пример запроса

```http
POST /ai/rewrite
Content-Type: application/json

{
  "text": "Сделай этот текст более вежливым: Отправь мне отчет сегодня"
}
```

#### cURL

```bash
curl -X POST http://localhost:8080/ai/rewrite \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Сделай этот текст более вежливым: Отправь мне отчет сегодня"
  }'
```
**[ОТВЕТ]**

```json
{
  "id": 1,
  "originalText": "Сделай этот текст более вежливым: Отправь мне отчет сегодня",
  "aiResponse": "Пожалуйста, отправьте мне отчет сегодня, когда вам будет удобно.",
  "createdAt": "2026-03-25T20:41:12.548"
}
```
---

### 2. POST `/ai/rewrite` — проверка пустого текста

#### Пример запроса

```http
POST /ai/rewrite
Content-Type: application/json

{
  "text": "   "
}
```

#### cURL

```bash
curl -X POST http://localhost:8080/ai/rewrite \
  -H "Content-Type: application/json" \
  -d '{
    "text": "   "
  }'
```

**[ОТВЕТ]**

```text
Text cannot be empty
```

> Ожидаемый статус: `400 Bad Request`

---

### 3. POST `/ai/rewrite` — еще один успешный пример

#### Пример запроса

```http
POST /ai/rewrite
Content-Type: application/json

{
  "text": "Сделай формулировку более формальной: Я не успеваю, перенесем встречу"
}
```

#### cURL

```bash
curl -X POST http://localhost:8080/ai/rewrite \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Сделай формулировку более формальной: Я не успеваю, перенесем встречу"
  }'
```
**[ОТВЕТ]**

```json
{
  "id": 2,
  "originalText": "Сделай формулировку более формальной: Я не успеваю, перенесем встречу",
  "aiResponse": "К сожалению, я не успеваю к назначенному времени. Предлагаю перенести встречу на более удобный момент.",
  "createdAt": "2026-03-25T20:44:09.173"
}
```
---

### 4. GET `/ai/history`

Возвращает всю историю запросов.

#### cURL

```bash
curl http://localhost:8080/ai/history
```
**[ОТВЕТ]**

```json
[
  {
    "id": 1,
    "originalText": "Сделай этот текст более вежливым: Отправь мне отчет сегодня",
    "aiResponse": "Пожалуйста, отправьте мне отчет сегодня, когда вам будет удобно.",
    "createdAt": "2026-03-25T20:41:12.548"
  },
  {
    "id": 2,
    "originalText": "Сделай формулировку более формальной: Я не успеваю, перенесем встречу",
    "aiResponse": "К сожалению, я не успеваю к назначенному времени. Предлагаю перенести встречу на более удобный момент.",
    "createdAt": "2026-03-25T20:44:09.173"
  }
]
```
---

### 5. GET `/ai/history/{id}` — успешное получение записи

#### cURL

```bash
curl http://localhost:8080/ai/history/1
```
**[ОТВЕТ]**

```json
{
  "id": 1,
  "originalText": "Сделай этот текст более вежливым: Отправь мне отчет сегодня",
  "aiResponse": "Пожалуйста, отправьте мне отчет сегодня, когда вам будет удобно.",
  "createdAt": "2026-03-25T20:41:12.548"
}
```
---

### 6. GET `/ai/history/{id}` — запись не найдена

#### cURL

```bash
curl http://localhost:8080/ai/history/999
```
**[ОТВЕТ]**

```text
Record not found
```

> Ожидаемый статус: `404 Not Found`
---

### 7. GET `/ai/history/{id}` — невалидный id

#### cURL

```bash
curl http://localhost:8080/ai/history/abc
```
**[ОТВЕТ]**

```text
Invalid id
```

> Ожидаемый статус: `400 Bad Request`
---

### 8. DELETE `/ai/history/{id}` — успешное удаление

#### cURL

```bash
curl -X DELETE http://localhost:8080/ai/history/1 -i
```
**[ОТВЕТ]**

```http
HTTP/1.1 204 No Content
```
---

### 9. DELETE `/ai/history/{id}` — запись не найдена

#### cURL

```bash
curl -X DELETE http://localhost:8080/ai/history/999 -i
```
**[ОТВЕТ]**

```text
Record not found
```

> Ожидаемый статус: `404 Not Found`
---

### 10. DELETE `/ai/history/{id}` — невалидный id

#### cURL

```bash
curl -X DELETE http://localhost:8080/ai/history/abc -i
```
**[ОТВЕТ]**

```text
Invalid id
```

> Ожидаемый статус: `400 Bad Request`
---

## Как выглядит запрос к OpenRouter

Сервис отправляет POST-запрос на:

```text
https://openrouter.ai/api/v1/chat/completions
```

С заголовками:

```text
Authorization: Bearer <API_KEY>
Content-Type: application/json
```

И минимальным телом вида:

```json
{
  "model": "openrouter/free",
  "messages": [
    {
      "role": "user",
      "content": "Сделай текст более вежливым"
    }
  ]
}
```
---

## Обработка ошибок

В проекте используется `StatusPages`.

### Поддерживаемые ошибки

- `IllegalArgumentException` -> `400 Bad Request`
- `BadRequestException` -> `400 Bad Request`
- `RecordNotFoundException` -> `404 Not Found`
- `OpenRouterException` -> `502 Bad Gateway`
- прочие ошибки -> `500 Internal Server Error`

