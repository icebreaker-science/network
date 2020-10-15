package science.icebreaker.network.keyword_merge.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import science.icebreaker.network.keyword_merge.comparators.SimilarityResult;
import science.icebreaker.network.keyword_merge.entities.Keyword;

public abstract class SimilarityResultLoader {
    protected List<SimilarityResult> similarity;
    private KeywordRepository keywordRepo;
    private List<Keyword> newKeywords;

    public SimilarityResultLoader(KeywordRepository keywordRepo) {
        this.similarity = new ArrayList<SimilarityResult>();
        this.newKeywords = new ArrayList<Keyword>();
        this.keywordRepo = keywordRepo;
    }

    public List<Keyword> getNewKeywords() {
        return newKeywords;
    }

    public abstract void load();

    public ListIterator<SimilarityResult> getIterator(int index) {
        return this.similarity.listIterator(index);
    }

    protected Keyword mergeKeyword(String keywordName) {
        Keyword existingKeyword = this.keywordRepo.getKeywordByName(keywordName);

        if(existingKeyword != null) return existingKeyword;
        else {
            Keyword newKeyword = new Keyword(keywordName, null, false);
            this.newKeywords.add(newKeyword);

            this.keywordRepo.addKeyword(newKeyword);
            return newKeyword;
        }
    }
}
