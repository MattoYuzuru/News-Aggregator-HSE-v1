package com.news.storage.inter;

import java.sql.SQLException;

public interface TagRepository {
    int getOrCreateTagId(String tagName) throws SQLException;
}
