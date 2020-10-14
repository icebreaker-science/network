package science.icebreaker.network.keyword_merge.programs;

import java.io.IOException;
import java.util.Arrays;
import java.util.ListIterator;

import chen.chaoran.data_manager.core.Startable;
import science.icebreaker.network.keyword_merge.comparators.SimilarityResult;
import science.icebreaker.network.keyword_merge.repository.KeywordRepository;
import science.icebreaker.network.keyword_merge.repository.LoadFromCSVSimilarityLoader;
import science.icebreaker.network.keyword_merge.repository.LoadFromDBRepository;
import science.icebreaker.network.keyword_merge.repository.SimilarityResultLoader;
import science.icebreaker.network.keyword_merge.resolvers.MergeInDBResolver;

public class MergeResultsInDBProgram implements Startable {
    private void merge(String[] mergeFilePaths) throws IOException {
        KeywordRepository keywordRepo = new LoadFromDBRepository();
        keywordRepo.load(); //load existing keywords from repo

        SimilarityResultLoader loader = new LoadFromCSVSimilarityLoader(Arrays.asList(mergeFilePaths), keywordRepo);
        loader.load(); //load similarity result CSV file

        MergeInDBResolver resolver = new MergeInDBResolver(keywordRepo);
        ListIterator<SimilarityResult> iterator = loader.getIterator(0);
        iterator.forEachRemaining(resolver::merge); //for each similarity result, resolve it

        resolver.resolveTransitiveMappings(); //if A refers to B and B refers to C change to A refers to C and B refers to C
        resolver.propagateResultsToDB(loader.getNewKeywords()); //Persist results into db
        resolver.end(); //cleanup DB inconsistencies
    }

    public void start(String[] args) throws IOException {
        this.merge(args);
    }
}
