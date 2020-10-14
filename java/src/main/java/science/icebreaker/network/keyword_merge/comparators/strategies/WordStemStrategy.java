package science.icebreaker.network.keyword_merge.comparators.strategies;

import opennlp.tools.stemmer.PorterStemmer;
import science.icebreaker.network.keyword_merge.comparators.SimilarityResult;
import science.icebreaker.network.keyword_merge.entities.Keyword;

/**
 * A similarity comparator which works by reducing both keywords to their stem
 * and checking whether both are the same
 * 
 * This is useful for detecting examples such as 'nanostructure', 'nanostructured' and 'nanostructures'
 */
public class WordStemStrategy implements SimilarityComparisonStrategy {

    private PorterStemmer stemmer;
    public WordStemStrategy() {
        this.stemmer = new PorterStemmer();
    }
    
    /**
     * Returns the similarity result of comparing two keywords by comparing the word stem
     * of the two keywords by using the Porter Stemming algorithm 
     * @see <a href="http://snowball.tartarus.org/algorithms/porter/stemmer.html">Porter Stemming Algorithm</a>
     * @param word1
     * @param word2
     * @return The similarity result
     */
    @Override
    public SimilarityResult compare(Keyword word1, Keyword word2) {
        String kw1 = word1.keyword;
        String kw2 = word2.keyword;
        String stemmedWord1 = this.stemmer.stem(kw1);
        String stemmedWord2 = this.stemmer.stem(kw2);

        if(stemmedWord1.equals(stemmedWord2)) {
            Keyword shorterWord = kw1.length() <= kw2.length() ? word1 : word2;
            return new SimilarityResult(word1, word2, shorterWord, true); // pick word1 wiki
        }
        else
            return new SimilarityResult(word1, word2, null, false);
    }
}
