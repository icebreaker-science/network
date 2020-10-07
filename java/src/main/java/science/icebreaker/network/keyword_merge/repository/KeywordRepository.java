package science.icebreaker.network.keyword_merge.repository;

import java.util.List;
import java.util.ListIterator;

import science.icebreaker.network.keyword_merge.entities.WikiData;
import science.icebreaker.network.keyword_merge.entities.Keyword;

/**
 * An interface that defines methods useful for accessing the state
 * of the db regarding keywords and wiki data
 */
public interface KeywordRepository {
    public abstract ListIterator<Keyword> getKeywordIterator(int idx);
    public abstract List<WikiData> getWikiDataByKeyword(Keyword keyword);
    public abstract void load();
    public abstract Keyword getKeywordByName(String name);
}
