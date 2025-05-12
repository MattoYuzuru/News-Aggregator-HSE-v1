package com.news.storage;

import java.sql.SQLException;

public interface ArticleTagLinker {
    void linkArticleTags(int articleId, int tagId) throws SQLException;
}
