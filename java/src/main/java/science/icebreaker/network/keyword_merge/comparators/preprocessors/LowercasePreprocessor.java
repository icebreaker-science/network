package science.icebreaker.network.keyword_merge.comparators.preprocessors;

import science.icebreaker.network.keyword_merge.entities.Keyword;
import science.icebreaker.network.keyword_merge.repository.KeywordRepository;

public class LowercasePreprocessor implements SimilarityComparatorPreprocessor {

    /**
     * Converts the keyword string to lowercase
     */
    @Override
    public Keyword preprocess(Keyword toProcess, Keyword Other, KeywordRepository repository) {
        return new Keyword(toProcess.keyword.toLowerCase(), toProcess.wikiData);
    }
    
}
