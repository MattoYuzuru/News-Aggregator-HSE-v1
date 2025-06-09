package com.news.storage.inter;

import java.sql.SQLException;

public interface ArticleTagLinker {
    void linkArticleTags(long articleId, int tagId) throws SQLException;
}
