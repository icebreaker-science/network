package science.icebreaker.network.keyword_merge.comparators.strategies;

import java.util.HashSet;
import java.util.Set;

import science.icebreaker.network.keyword_merge.comparators.SimilarityResult;
import science.icebreaker.network.keyword_merge.entities.Keyword;

public class AliasesStrategy implements SimilarityComparisonStrategy {

    /**
     * Check if there are overlapping aliases
     */
    @Override
    public SimilarityResult compare(Keyword word1, Keyword word2) {
        Set<String> kw1Aliases = word1.getAliases();
        Set<String> kw2Aliases = word2.getAliases();
        Set<String> intersection = new HashSet<String>(kw1Aliases);
        intersection.retainAll(kw2Aliases);

        if(intersection.size() > 0) {
            Keyword longerWord = word1.keyword.length() >= word2.keyword.length() ? word1 : word2;
            return new SimilarityResult(word1, word2, longerWord, true);
        } else {
            return new SimilarityResult(word1, word2, null, false);
        }
    }    
}
