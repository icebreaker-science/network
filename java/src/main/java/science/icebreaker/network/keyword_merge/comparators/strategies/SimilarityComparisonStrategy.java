package science.icebreaker.network.keyword_merge.comparators.strategies;

import science.icebreaker.network.keyword_merge.comparators.SimilarityResult;
import science.icebreaker.network.keyword_merge.entities.Keyword;

@FunctionalInterface
public interface SimilarityComparisonStrategy {
    SimilarityResult compare(Keyword word1, Keyword word2);

    public static final SimilarityComparisonStrategy RemoveSplitChars = new RemoveSplitCharsStrategy();
    public static final SimilarityComparisonStrategy Subset = new SubsetStrategy();
    public static final SimilarityComparisonStrategy WordStem = new WordStemStrategy();
    public static final SimilarityComparisonStrategy Aliases = new AliasesStrategy();
}
