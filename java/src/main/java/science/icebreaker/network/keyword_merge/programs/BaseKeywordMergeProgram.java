package science.icebreaker.network.keyword_merge.programs;

import java.io.IOException;

import chen.chaoran.data_manager.core.Startable;
import science.icebreaker.network.keyword_merge.comparators.SimilarityComparator;
import science.icebreaker.network.keyword_merge.comparators.SimilarityComparatorUnit;
import science.icebreaker.network.keyword_merge.comparators.preprocessors.SimilarityComparatorPreprocessor;
import science.icebreaker.network.keyword_merge.comparators.strategies.SimilarityComparisonStrategy;
import science.icebreaker.network.keyword_merge.comparison.ComparatorSpecification;
import science.icebreaker.network.keyword_merge.comparison.KeywordComparison;
import science.icebreaker.network.keyword_merge.repository.KeywordRepository;
import science.icebreaker.network.keyword_merge.repository.LoadFromDBRepository;
import science.icebreaker.network.keyword_merge.resolvers.SimilarityResultResolver;
import science.icebreaker.network.keyword_merge.resolvers.WriteToFileResolver;

public class BaseKeywordMergeProgram implements Startable {
    private void merge(String mergeFilePath) throws IOException {
        
        KeywordRepository repository = new LoadFromDBRepository(); // Get keyword directly from DB

        SimilarityComparator strategy = new SimilarityComparatorUnit(
            SimilarityComparisonStrategy.RemoveSplitChars); // Decide merge candidates based on the decided strategy

        int flushResultsEach = 50; // How many merge results should be completed for the results to be flushed
        SimilarityResultResolver resolver = new WriteToFileResolver(mergeFilePath, flushResultsEach); // Write merge candidates to a CSV file

        ComparatorSpecification compareSpec = new ComparatorSpecification(strategy, resolver); // pair both the merge comparator and the merge resolver
        compareSpec.addPreprocessor(SimilarityComparatorPreprocessor.toLower); // convert keywords to lowercase before comparison

        //Main functionality module
        KeywordComparison spec = new KeywordComparison(repository);
        spec.addComparatorSpec(compareSpec);
        spec.start();
    }

    public void start(String[] args) throws IOException {
        this.merge(args[0]);
    }
}
