package science.icebreaker.network.keyword_merge.resolvers;

import science.icebreaker.network.keyword_merge.entities.Keyword;

/**
 * Provides functionality for resolving merge results generated from comparators
 */
public interface SimilarityResultResolver {
	void merge(Keyword word1, Keyword word2, Keyword origin);
	void end();
}
