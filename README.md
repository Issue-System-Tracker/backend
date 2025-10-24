## Настройка окружения

1. Создайте файл `.env` в корне проекта (опционально). Пример:

```dotenv
# База данных
DB_NAME=issue_tracker_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_URL=jdbc:postgresql://postgres:5432/issue_tracker_db

# JWT
JWT_SECRET=your_jwt_secret
JWT_ACCESS_EXPIRATION_MIN=60
JWT_REFRESH_EXPIRATION_DAYS=30
```

2. Сборка и запуск контейнеров:
    ``` 
   docker-compose up --build
    ```

