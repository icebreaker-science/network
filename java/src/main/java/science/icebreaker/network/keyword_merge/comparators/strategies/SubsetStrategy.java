package science.icebreaker.network.keyword_merge.comparators.strategies;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import science.icebreaker.network.keyword_merge.comparators.SimilarityResult;
import science.icebreaker.network.keyword_merge.entities.Keyword;

/**
 * A comparison strategy function which works by deciding if 2 keywords are identical if one of the
 * keyword subwords is a subset of another.
 * 
 * This is useful for detecting examples such as 'simulation model' and 'model simulation'
 */
public class SubsetStrategy implements SimilarityComparisonStrategy {

    @Override
    public SimilarityResult compare(Keyword word1, Keyword word2) {
        String kw1 = word1.keyword;
        String kw2 = word2.keyword;
        Set<String> word1Fragments = new HashSet<String>(Arrays.asList(kw1.split(" ")));
        Set<String> word2Fragments = new HashSet<String>(Arrays.asList(kw2.split(" ")));
        if(isSubset(word1Fragments, word2Fragments)) {
            if(word1Fragments.size() >= word2Fragments.size())
                return new SimilarityResult(word1, word2, word1, true);
            else
                return new SimilarityResult(word1, word2, word2, true);
        } 
        else return new SimilarityResult(word1, word2, null, false);
    }

    private boolean isSubset(Set<String> word1Fragments, Set<String> word2Fragments) {
        if(word1Fragments.size() < word2Fragments.size()) return isSubset(word2Fragments, word1Fragments);
        return word2Fragments.containsAll(word1Fragments);
	}
}
