package science.icebreaker.network.keyword_merge.entities;

public class Keyword {
    public String keyword;
    public WikiData wikiData;

    public Keyword(String keyword, WikiData wikiData) {
        this.keyword = keyword;
        this.wikiData = wikiData;
    }

    @Override
    public String toString() {
        return this.keyword;
    }
}
