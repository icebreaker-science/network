package science.icebreaker.network.keyword_merge.comparators;

import opennlp.tools.stemmer.PorterStemmer;

public class WordStemComparator extends SimilarityComparator {

    private PorterStemmer stemmer;
    public WordStemComparator() {
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
    SimilarityResult sCompare(String word1, String word2) {
        String stemmedWord1 = this.stemmer.stem(word1);
        String stemmedWord2 = this.stemmer.stem(word2);

        if(stemmedWord1 == stemmedWord2)
            return new SimilarityResult(stemmedWord1, true);
        else
            return new SimilarityResult(null, false);
    }
}
