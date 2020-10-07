package science.icebreaker.network.keyword_merge.comparators;

import science.icebreaker.network.keyword_merge.entities.Keyword;

/**
 * Represents the result of comparing 2 keywords for similarity
 */
public class SimilarityResult {
    // Useful for deciding a common root keyword(might come in handy when inserting new data)
    public final Keyword origin;
    public final Boolean isSimilar;

    public SimilarityResult(Keyword origin, Boolean isSimilar) {
        this.origin = origin;
        this.isSimilar = isSimilar;
    }

    public boolean equals(SimilarityResult result) {
        return origin.equals(result.origin) && (this.isSimilar == result.isSimilar);
    }
}
