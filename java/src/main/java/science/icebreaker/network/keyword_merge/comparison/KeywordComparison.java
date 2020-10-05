package science.icebreaker.network.keyword_merge.comparison;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import science.icebreaker.network.keyword_merge.entities.Keyword;
import science.icebreaker.network.keyword_merge.repository.KeywordRepository;

/**
 * Holds the main functionality for starting a keyword comparison job
 */
public class KeywordComparison {
    
    KeywordRepository keywordRepoistory;
    List<ComparatorSpecification> comparatorSpecs;

    public KeywordComparison(KeywordRepository keywordRepoistory) {
        this.keywordRepoistory = keywordRepoistory;
        this.comparatorSpecs = new ArrayList<ComparatorSpecification>();
    }

    public void addComparatorSpec(ComparatorSpecification spec) {
        this.comparatorSpecs.add(spec);
    }

    public void start() {
        // apply each comparator spec
        this.comparatorSpecs.forEach(spec -> {
            int total = 0; //used for reporting progress

            // Get 2 iterators for comparing all keywords against each other
            for (ListIterator<Keyword> outterIterator = keywordRepoistory.getKeywordIterator(0); outterIterator.hasNext();) {
                Keyword word1 = outterIterator.next();

                // Start at outterIterator.nextIndex() to avoid comparing a keyword against itself or
                // comparing keywords A and B in both possible ways i.e. A,B and B,A
                for(ListIterator<Keyword> innerIterator = keywordRepoistory.getKeywordIterator(outterIterator.nextIndex()); innerIterator.hasNext();) {
                    Keyword word2 = innerIterator.next();
                    spec.compare(word1, word2, keywordRepoistory);
                    total++;
                    if(total % 1000000 == 0) System.out.println("Compared: " + total);
                }
            }
            spec.finish();
        });
    }
}
