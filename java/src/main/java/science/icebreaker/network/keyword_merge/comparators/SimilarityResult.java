package science.icebreaker.network.keyword_merge.comparators;

import science.icebreaker.network.keyword_merge.entities.Keyword;

/**
 * Represents the result of comparing 2 keywords for similarity
 */
public class SimilarityResult {
    public final Keyword kw1;
    public final Keyword kw2;
    // Useful for deciding a common root keyword(might come in handy when inserting new data)
    public final Keyword origin;
    public final Boolean isSimilar;

    public SimilarityResult(Keyword kw1, Keyword kw2, Keyword origin, Boolean isSimilar) {
        this.kw1 = kw1;
        this.kw2 = kw2;
        this.origin = origin;
        this.isSimilar = isSimilar;
    }

    public boolean equals(SimilarityResult result) {
        return 
            kw1.equals(result.kw1)
            && kw2.equals(result.kw2)
            && origin.equals(result.origin) 
            && (this.isSimilar == result.isSimilar);
    }
}
