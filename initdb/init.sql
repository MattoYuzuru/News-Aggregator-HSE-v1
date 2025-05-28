-- initdb/init.sql
CREATE TABLE articles
(
    id           SERIAL PRIMARY KEY,
    title        TEXT,
    author       TEXT,
    url          TEXT UNIQUE NOT NULL,
    content      TEXT,
    summary      TEXT,
    region       TEXT,
    source_name  TEXT,
    image_url    TEXT,
    language     TEXT,
    status       TEXT        DEFAULT 'RAW',
    published_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tags
(
    id   SERIAL PRIMARY KEY,
    name TEXT UNIQUE NOT NULL
);

CREATE TABLE article_tags
(
    article_id INTEGER REFERENCES articles (id) ON DELETE CASCADE,
    tag_id     INTEGER REFERENCES tags (id) ON DELETE CASCADE,
    PRIMARY KEY (article_id, tag_id)
);

CREATE INDEX IF NOT EXISTS idx_articles_status ON articles (status);
CREATE INDEX IF NOT EXISTS idx_articles_published_at ON articles (published_at);
CREATE INDEX IF NOT EXISTS idx_articles_source_name ON articles (source_name);
