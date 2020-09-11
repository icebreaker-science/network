package science.icebreaker.network.keyword_merge.comparators;

/**
 * Represents the result of comparing 2 keywords for similarity
 */
public class SimilarityResult {
    // Useful for deciding a common root keyword(might come in handy when inserting new data)
    public final String origin;
    public final Boolean isSimilar;

    public SimilarityResult(String origin, Boolean isSimilar) {
        this.origin = origin;
        this.isSimilar = isSimilar;
    }

}
