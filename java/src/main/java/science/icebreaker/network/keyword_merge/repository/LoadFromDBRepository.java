package science.icebreaker.network.keyword_merge.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import science.icebreaker.network.Database;
import science.icebreaker.network.keyword_merge.entities.Keyword;
import science.icebreaker.network.keyword_merge.entities.WikiData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Loads all required data for merging keywords
 * from db and keeps it in memory for usage
 */
public class LoadFromDBRepository implements KeywordRepository {

    private List<Iterator<Keyword>> keywordIterators; // may be used in the future for checkpoints
    private ArrayList<Keyword> keywords;
    private Map<String, Keyword> keywordMap;
    private boolean loaded;
    private boolean loadAliases;
    private Connection conn;
    private PreparedStatement getAliasesStmt;

    public LoadFromDBRepository(boolean loadAliases) throws SQLException {
        this.keywords = new ArrayList<Keyword>();
        this.keywordIterators = new ArrayList<Iterator<Keyword>>();
        this.keywordMap = new HashMap<String, Keyword>();
        this.loaded = false;
        this.loadAliases = loadAliases;
        this.conn = Database.getConnection();
        this.getAliasesStmt = conn.prepareStatement(
              "SELECT (alias_elements->>'value')::text as kalias "
            + "FROM public.keyword__wikidata, jsonb_array_elements(aliases) alias_elements "
            + "WHERE keyword=? AND aliases IS NOT NULL;"
        );
    }

    @Override
    public ListIterator<Keyword> getKeywordIterator(int idx) {
        if(!this.loaded) this.load();
        ListIterator<Keyword> keywordIterator = keywords.listIterator(idx);
        this.keywordIterators.add(keywordIterator);
        return keywordIterator;
    }

    @Override
    public List<WikiData> getWikiDataByKeyword(Keyword keyword) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Keyword getKeywordByName(String name) {
        return this.keywordMap.get(name);
    }

    @Override
    public void load() {
        loadKeywords();
        this.loaded = true;
    }

    // TODO: load wikidata as well and associate it
    private void loadKeywords() {
        try {
            int counter = 0;
            System.out.println("Loading Keywords");
            Statement stmt = conn.createStatement();
            ResultSet rs;
            rs = stmt.executeQuery("SELECT name FROM keyword");
            while (rs.next()) {
                String keywordName = rs.getString("name");
                Keyword keyword = new Keyword(keywordName, null, this.loadAliases);
                
                //get all aliases for the keyword
                if(this.loadAliases) {
                    this.getAliasesStmt.setString(1, keywordName);
                    ResultSet aliasesRS = this.getAliasesStmt.executeQuery();
                    while(aliasesRS.next()) {
                        String curAlias = aliasesRS.getString("kalias");
                        keyword.addAlias(curAlias.toLowerCase());
                    }
                }

                this.keywords.add(keyword);
                this.keywordMap.put(keywordName, keyword);
                counter++;
                if(counter % 100 == 0) System.out.println("loaded keywords: " + counter);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Done Loading Keywords");
    }

    @Override
    public void addKeyword(Keyword keyword) {
        this.keywords.add(keyword);
        this.keywordMap.put(keyword.keyword, keyword);
    }
    
}
