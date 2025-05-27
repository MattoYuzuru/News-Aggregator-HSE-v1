# News Aggregator V.1

## 📌 Description

A news aggregator developed as a 1st-year course project for the *Introduction to Java* subject.

It automatically **collects**, **processes**, **analyzes**, and **filters** news from various sources.

## 🚀 Features
- **News collection** from specified sources (XML, web page parsing)
- Configurable list of news sources
- **Duplicate removal** and **content cleaning** (e.g., ad stripping)
- Tag-based classification
- Tag generation and article summarization
- Saving articles to a database
- Filtering by date, search by tags, content, and more
- Console-based interface with command support
- **Export** to CSV, JSON, and HTML
- **Automatic updates** on a schedule
- Basic statistics

## 🛠️ Technologies

- **Backend**: Java 21
- **Database**: PostgreSQL
- **Tools**: Docker, Maven
- **Parsing**: JSoup
- **AI/ML**: Ollama (local, for now)

## 📦 Setup and Running

1. Clone the repository:
   ```shell
   git clone https://github.com/MattoYuzuru/News-Aggregator-HSE-v1.git
   cd News-Aggregator-HSE-v1
   ```
2. Run with Docker:
    ```shell
    docker-compose up --build
    ```
3. Run manually:

   ```shell
   mvn clean install
   java -cp target/news-parser-1.0.jar com.news.Main
   ```

## 📝 Лицензия
[BSD 2-Clause License](LICENSE)