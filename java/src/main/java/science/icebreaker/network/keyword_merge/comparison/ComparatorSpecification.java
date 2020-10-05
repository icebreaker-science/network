package science.icebreaker.network.keyword_merge.comparison;

import java.util.ArrayList;
import java.util.List;

import science.icebreaker.network.keyword_merge.comparators.SimilarityComparator;
import science.icebreaker.network.keyword_merge.comparators.SimilarityResult;
import science.icebreaker.network.keyword_merge.comparators.preprocessors.SimilarityComparatorPreprocessor;
import science.icebreaker.network.keyword_merge.entities.Keyword;
import science.icebreaker.network.keyword_merge.repository.KeywordRepository;
import science.icebreaker.network.keyword_merge.resolvers.SimilarityResultResolver;

/**
 * Combines modules for preprocessing, comparing and resolving the result of comparing keywords
 */
public class ComparatorSpecification {

    private final List<SimilarityComparatorPreprocessor> preprocessors;
    private final SimilarityComparator comparator;
    private final SimilarityResultResolver resolver;

    public ComparatorSpecification(SimilarityComparator comparator, SimilarityResultResolver resolver) {
        this.comparator = comparator;
        this.resolver = resolver;
        this.preprocessors = new ArrayList<SimilarityComparatorPreprocessor>();
    }

    public void addPreprocessor(SimilarityComparatorPreprocessor preprocessor) {
        this.preprocessors.add(preprocessor);
    }

    public void compare(Keyword word1, Keyword word2, KeywordRepository repository) {
        //preprocess
        for(SimilarityComparatorPreprocessor preprocessor : this.preprocessors) {
            Keyword tmp1 = preprocessor.preprocess(word1, word2, repository);
            Keyword tmp2 = preprocessor.preprocess(word2, word1, repository);
            word1 = tmp1;
            word2 = tmp2;
        }

        //compare
        SimilarityResult res = comparator.compare(word1, word2, repository);

        //resolve
        if(res.isSimilar) resolver.merge(word1, word2, res.origin);
    }

    public void finish() {
        this.resolver.end();
    }
}
