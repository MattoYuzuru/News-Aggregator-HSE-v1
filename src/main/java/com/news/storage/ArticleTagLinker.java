package com.news.storage;

import java.sql.SQLException;

public interface ArticleTagLinker {
    void linkArticleTags(long articleId, int tagId) throws SQLException;
}
