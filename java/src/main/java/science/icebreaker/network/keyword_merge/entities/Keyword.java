package science.icebreaker.network.keyword_merge.entities;

import java.util.Set;
import java.util.HashSet;

public class Keyword {
    public String keyword;
    public WikiData wikiData;
    public Set<String> aliases;

    public Keyword(String keyword, WikiData wikiData, boolean loadAliases) {
        this.keyword = keyword;
        this.wikiData = wikiData;
        if(loadAliases) this.aliases = new HashSet<String>();
    }

    @Override
    public String toString() {
        return this.keyword;
    }

    @Override
    public int hashCode() {
        return this.keyword.hashCode();
    }

    public Set<String> getAliases() {
        return this.aliases;
    }

    public void addAlias(String alias) {
        this.aliases.add(alias);
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof Keyword) && this.keyword == ((Keyword)o).keyword;
    }
}
