package science.icebreaker.network.keyword_merge.comparators;

import science.icebreaker.network.keyword_merge.entities.Keyword;
import science.icebreaker.network.keyword_merge.repository.KeywordRepository;

/**
 * Holds functionality for deciding of 2 keywords are similar or not
 */
public abstract class SimilarityComparator {
    
    public abstract SimilarityResult compare(Keyword word1, Keyword word2, KeywordRepository repository);
    
    public SimilarityComparator or(SimilarityComparator other) {
        return new OrSimilarityComparator(this, other);
    }

    public SimilarityComparator and(SimilarityComparator other) {
        return new AndSimilarityComparator(this, other);
    }
}