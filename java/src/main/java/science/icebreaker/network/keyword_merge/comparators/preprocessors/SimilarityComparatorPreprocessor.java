package science.icebreaker.network.keyword_merge.comparators.preprocessors;

import science.icebreaker.network.keyword_merge.entities.Keyword;
import science.icebreaker.network.keyword_merge.repository.KeywordRepository;

/**
 * Provides a functional interface for implementing various
 * keyword preprocessors used during merging of keywords
 */
public interface SimilarityComparatorPreprocessor {
    Keyword preprocess(Keyword toProcess, Keyword Other, KeywordRepository repository);

    /**
     * TODO: 
     * use abbreviation dictionary preprocessor
     * if the wikidata data is reliable, reduce each word to its base alias parent using
     * a preprocessor and implement a corresponding comparator
     */
    public static final SimilarityComparatorPreprocessor toLower = new LowercasePreprocessor();
}
