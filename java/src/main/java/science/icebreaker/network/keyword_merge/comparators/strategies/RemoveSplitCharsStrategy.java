package science.icebreaker.network.keyword_merge.comparators.strategies;

import java.util.Arrays;
import java.util.List;

import science.icebreaker.network.keyword_merge.comparators.SimilarityResult;
import science.icebreaker.network.keyword_merge.entities.Keyword;

/**
 * A comparison strategy function which works by replacing all occurences of a split character like 
 * ' ', '-' or '_' with a neutral string. 
 * 
 * This is useful for detecting examples such 'watersamples' and 'water samples'
 */
public class RemoveSplitCharsStrategy implements SimilarityComparisonStrategy {
    private static final List<String> SPLIT_STRS = Arrays.asList(" ", "-" , "_");
    private static final String NEUTRAL_STR = "";

    @Override
    public SimilarityResult compare(Keyword word1, Keyword word2) {
        String kw1 = word1.keyword;
        String kw2 = word2.keyword;
        for(String str : SPLIT_STRS) {
            kw1 = kw1.replaceAll(str, NEUTRAL_STR);
            kw2 = kw2.replaceAll(str, NEUTRAL_STR);
        }
        
        if(kw1.equals(kw2)) {
            Keyword longerWord = word1.keyword.length() >= word2.keyword.length() ? word1 : word2;
            return new SimilarityResult(longerWord, true);
        }
        else return new SimilarityResult(null, false);
    }

}
