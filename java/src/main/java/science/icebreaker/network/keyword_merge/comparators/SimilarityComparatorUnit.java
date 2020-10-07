package science.icebreaker.network.keyword_merge.comparators;

import science.icebreaker.network.keyword_merge.comparators.strategies.SimilarityComparisonStrategy;
import science.icebreaker.network.keyword_merge.entities.Keyword;
import science.icebreaker.network.keyword_merge.repository.KeywordRepository;

/**
 * Decides a similarity comparison between 2 keywords based on the strategy provided to it
 */
public class SimilarityComparatorUnit extends SimilarityComparator {
    
    private final SimilarityComparisonStrategy strategy;

    public SimilarityComparatorUnit(SimilarityComparisonStrategy strategy) {
        this.strategy = strategy;
    }

    public SimilarityComparisonStrategy getStrategy() {
        return this.strategy;
    }

    public SimilarityResult compare(Keyword word1, Keyword word2, KeywordRepository repository) {
        SimilarityResult result; //TODO: if needed save repeated comparisons
        if(word1.equals(word2))
            result = new SimilarityResult(word1, true);
        else
            result = strategy.compare(word1, word2);

        return result;
    }
}
