package science.icebreaker.network.keyword_merge.comparators;

import jdk.jfr.Experimental;
import science.icebreaker.network.keyword_merge.entities.Keyword;
import science.icebreaker.network.keyword_merge.repository.KeywordRepository;

//TODO: decide if this is useful

/**
 * A similarity comparator which returns the opposite of the similarity comparison result
 */
@Experimental
public class NotSimilarityComparator extends SimilarityComparator {
    private final SimilarityComparator comp;

    public NotSimilarityComparator(SimilarityComparator comp) {
        this.comp = comp;
    }

    @Override
    public SimilarityResult compare(Keyword word1, Keyword word2, KeywordRepository repository) {
        SimilarityResult result = comp.compare(word1, word2, repository);
        if(result.isSimilar) return new SimilarityResult(word1, word2, null, false);
        else return new SimilarityResult(word1, word2, word1, true); //pick any word
    }
}
