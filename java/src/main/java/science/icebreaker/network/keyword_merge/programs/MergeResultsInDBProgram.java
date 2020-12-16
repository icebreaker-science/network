package science.icebreaker.network.keyword_merge.programs;

import java.util.Arrays;
import java.util.ListIterator;

import chen.chaoran.data_manager.core.Startable;
import science.icebreaker.network.keyword_merge.comparators.SimilarityResult;
import science.icebreaker.network.keyword_merge.repository.KeywordRepository;
import science.icebreaker.network.keyword_merge.repository.LoadFromCSVSimilarityLoader;
import science.icebreaker.network.keyword_merge.repository.LoadFromDBRepository;
import science.icebreaker.network.keyword_merge.repository.SimilarityResultLoader;
import science.icebreaker.network.keyword_merge.resolvers.MergeInDBResolver;

/**
 * Merges keywords according to the given data
 * IMPORTANT: 
 *          - Before starting make sure that RELATED_TO weights are set to be lists
 *          - If RELATED_TO weights are not consistent use the script provided to fix them
 *          - After merging run the script to merge bi-directional RELATED_TO edges to avoid having inconsistent edge IDs
 *          - Restore RELATED_TO references to strings using th script provided
 *      - All scripts are under the keyword_merge/neo4jscripts directory
 * 
 */
public class MergeResultsInDBProgram implements Startable {
    private void merge(String[] mergeFilePaths) throws Exception {
        KeywordRepository keywordRepo = new LoadFromDBRepository(false);
        keywordRepo.load(); //load existing keywords from repo

        SimilarityResultLoader loader = new LoadFromCSVSimilarityLoader(Arrays.asList(mergeFilePaths), keywordRepo);
        loader.load(); //load similarity result CSV file

        MergeInDBResolver resolver = new MergeInDBResolver(keywordRepo);
        ListIterator<SimilarityResult> iterator = loader.getIterator(0);
        iterator.forEachRemaining(resolver::merge); //for each similarity result, resolve it

        // use to detect cycles in given merge data
        // resolver.findCycles();
        resolver.resolveTransitiveMappings(); //if A refers to B and B refers to C change to A refers to C and B refers to C
        // used when resuming
        // resolver.syncKeywords();
        resolver.propagateResultsToDB(loader.getNewKeywords()); //Persist results into db
        resolver.end(); //cleanup DB inconsistencies
    }

    public void start(String[] args) throws Exception {
        this.merge(args);
    }
}
