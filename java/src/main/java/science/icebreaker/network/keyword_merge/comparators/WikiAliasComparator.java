package science.icebreaker.network.keyword_merge.comparators;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;

import science.icebreaker.network.Database;

public class WikiAliasComparator extends SimilarityComparator {

    private final PreparedStatement statement;

    public WikiAliasComparator() {
        try {
            Connection conn = Database.getConnection();
            statement = conn.prepareStatement(
                "SELECT wikidata.data -> 'aliases' -> 'en' " +
                "FROM public.keyword__wikidata as keyword INNER JOIN public.wikidata as wikidata ON keyword.wikidata = wikidata.wikidata_id " +
                "WHERE keyword.keyword = ?"
            );
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }
    }

    /**
     * Get the similarity result between 2 words based on their aliases in the wikidata
     * @param word1
     * @param word2
     * @return The similarity result
     */
    @Override
    SimilarityResult sCompare(String word1, String word2) {
        try {
            Set<String> commonAliases;
            commonAliases = getAliases(word1);
            commonAliases.retainAll(getAliases(word2));

            if(commonAliases.size() > 0)
                return new SimilarityResult(commonAliases.iterator().next(), true);
            else
                return new SimilarityResult(null, false);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public Set<String> getAliases(String word) throws SQLException {
        Set<String> set = new HashSet<String>();
        statement.setString(1, word);
        ResultSet result = statement.executeQuery();
        while(result.next()) {
            JSONArray arr = new JSONArray(result.getString(1));
            arr.toList().stream().map(map -> (String)((Map)map).get("value")).forEach(set::add);
        }
        return set;
    }
    
}
