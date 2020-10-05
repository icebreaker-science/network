package science.icebreaker.network.keyword_merge.comparators;

import science.icebreaker.network.keyword_merge.entities.Keyword;
import science.icebreaker.network.keyword_merge.repository.KeywordRepository;

/**
 * A similarity comparator which decides merges based on one of the embedded similarity comparators into it
 */
public class OrSimilarityComparator extends SimilarityComparator {
    private final SimilarityComparator comp1;
    private final SimilarityComparator comp2;

    public OrSimilarityComparator(SimilarityComparator comp1, SimilarityComparator comp2) {
        this.comp1 = comp1;
        this.comp2 = comp2;
    }

    @Override
    public SimilarityResult compare(Keyword word1, Keyword word2, KeywordRepository repository) {
        SimilarityResult res1 = comp1.compare(word1, word2, repository);
        if(res1.isSimilar) return res1;
        SimilarityResult res2 = comp2.compare(word1, word2, repository);
        return res2;
    }
}
