# News Aggregator V.1

## 📌 Описание
Новостной агрегатор, сделанный для защиты проекта 1ого курса по предмету Введение в Java,

который автоматически **собирает**, **обрабатывает**, **анализирует** и **фильтрует** новости из различных источников.

## 🚀 Функционал
- **Сбор** новостей из указанных источников (API, парсинг веб-страниц).
- **Исключение дубликатов** и **очистка контента** от рекламы.
- Классификация по категориям и ключевым словам.
- Аналитика (выявление трендов, подсчет упоминаний персон).
- Фильтрация и поиск по дате, категории, ключевым словам.
- **API** для веб-интерфейса.
- **Автоматическое обновление новостей** через заданные интервалы.
- **Экспорт** новостей в CSV, JSON, HTML.

## 🛠️ Технологии (В работе)
- **Backend**: Java 21, Spring Boot, Spring MVC, Hibernate
- **Database**: PostgreSQL
- **Tools**: Docker, Maven
- **Парсинг**: JSoup, Apache HttpClient
- **AI/ML**: OpenAI API, Hugging Face

## 📦 Установка и запуск

1. Клонирование репозитория
```sh

git clone https://github.com/yourusername/news-aggregator.git
cd news-aggregator
```
2. Запуск через Docker
```sh

docker-compose up --build
```
3. Запуск вручную
```sh
mvn clean install  
java -jar target/news-aggregator.jar
```

## 📂 Структура проекта
```text
news-aggregator/
│── src/main/java/com/news/
│   │── parser/       # Сбор новостей (парсинг, API-агрегация)
│   │── processor/    # Обработка данных (фильтрация, NLP)
│   │── storage/      # Работа с БД (JPA, Hibernate, PostgreSQL)
│   │── search/       # Поиск, индексация
│   │── analytics/    # Аналитика (тренды, популярные темы)
│   │── api/          # REST API
│── src/main/resources/
│   │── application.yml  # Конфигурация Spring Boot
│── docker-compose.yml
│── pom.xml           # Зависимости Maven
│── README.md
```

## 📝 Лицензия
[BSD 2-Clause License](LICENSE)

