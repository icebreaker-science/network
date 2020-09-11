package science.icebreaker.network.keyword_merge.comparators;

public abstract class SimilarityComparator {

    public SimilarityResult compare(String word1, String word2) {
        if(word1.equals(word2))
            return new SimilarityResult(word1, true);
        else
            return sCompare(word1, word2);
    }

    abstract SimilarityResult sCompare(String word1, String word2);
}
