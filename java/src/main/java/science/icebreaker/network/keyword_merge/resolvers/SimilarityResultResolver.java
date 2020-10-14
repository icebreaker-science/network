package science.icebreaker.network.keyword_merge.resolvers;

import science.icebreaker.network.keyword_merge.comparators.SimilarityResult;

/**
 * Provides functionality for resolving merge results generated from comparators
 */
public interface SimilarityResultResolver {
    void merge(SimilarityResult result);
    void end();
}
