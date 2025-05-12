package com.news.storage;

import java.sql.SQLException;

public interface TagRepository {
    int getOrCreateTagId(String tagName) throws SQLException;
}
