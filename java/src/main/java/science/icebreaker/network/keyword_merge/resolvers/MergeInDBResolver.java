package science.icebreaker.network.keyword_merge.resolvers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.Result;

import science.icebreaker.network.Database;
import science.icebreaker.network.Graph;
import science.icebreaker.network.keyword_merge.comparators.SimilarityResult;
import science.icebreaker.network.keyword_merge.entities.Keyword;
import science.icebreaker.network.keyword_merge.repository.KeywordRepository;

public class MergeInDBResolver implements SimilarityResultResolver {
    /**
     * Used for intermediate mapping. Ex: if keyword B is decided to be merged into
     * keyword A and later on, Keyword C is decided to be merged into keyword B,
     * this map can be used to map it to C instead
     */
    private Map<Keyword, Keyword> refersTo; // used for intermediate mapping
    private KeywordRepository keywordRepo;

    private PreparedStatement putKeywordRefStmt;
    private PreparedStatement addNewKeywordStmt;
    private PreparedStatement updateWeightStmt;

    public MergeInDBResolver(KeywordRepository keywordRepo) {
        this.keywordRepo = keywordRepo;
        this.refersTo = new HashMap<Keyword, Keyword>();
        try {
            Connection conn = Database.getConnection();
            putKeywordRefStmt = conn.prepareStatement(
                "UPDATE keyword " 
                + "SET refers_to=? "
                + "WHERE name=?;"
            );
            addNewKeywordStmt = conn.prepareStatement(
                "INSERT INTO keyword(name, weight) "
                + "VALUES(?,0);"
            );
            updateWeightStmt = conn.prepareStatement(
                "UPDATE keyword "
                + "SET weight=? "
                + "WHERE name=?;"
            );
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }
    }

    public void merge(SimilarityResult result) {
        if (!result.isSimilar)
            return;

        if (result.origin.equals(result.kw1)) {
            this.mapWord(result.kw2, result.kw1);
        } else if (result.origin.equals(result.kw2)) {
            this.mapWord(result.kw1, result.kw2);
        } else {
            this.mapWord(result.kw1, result.origin);
            this.mapWord(result.kw2, result.origin);
        }
    }

    /**
     * Maps word1 to word2 i.e. word2 is the base word and word1 is the alias
     * 
     * @param word1
     * @param word2
     */
    public void mapWord(Keyword word1, Keyword word2) {
        this.refersTo.put(word1, word2);
    }

    /**
     * When keyword A is set to refer to keyword B and later on keyword B is set to
     * refer to keyword C, A should be set to refer to C.
     * 
     * check if there are no transitive refersTo entries and adjust any if found.
     */
    public void resolveTransitiveMappings() {
        for (Keyword word1 : this.refersTo.keySet()) {
            Keyword parent = this.refersTo.get(word1);
            if (parent == null)
                continue; // root keyword

            Keyword ancestor = getKeywordAncestor(parent);
            if (parent != ancestor)
                mapWord(word1, ancestor);
        }
    }

    /**
     * Gets the base keyword in a refersTo relation chain.
     * 
     * @param word the word to get the ancestor of
     * @return the base keyword
     */
    private Keyword getKeywordAncestor(Keyword word) {
        Keyword next = this.refersTo.getOrDefault(word, word);
        if (next == word)
            return next;
        else
            return getKeywordAncestor(next);
    }

    public void propagateResultsToDB(List<Keyword> newKeywords) {
        // add non existing keywords
        for (Keyword newKeyword : newKeywords) {
            // sql side
            try {
                this.addNewKeywordStmt.setString(1, newKeyword.keyword);
                this.addNewKeywordStmt.execute();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try (Session session = Graph.getInstance().getSession()) {
                session.writeTransaction(new TransactionWork<Void>() {
                    @Override
                    public Void execute(Transaction tx) {
                        Map<String, Object> params = new HashMap<>();
                        params.put("name", newKeyword.keyword);
                        tx.run("CREATE (:Topic {name:$name, weight:0})", params);
                        return null;
                    }

                });
            }

        }

        for (Entry<Keyword, Keyword> entry : this.refersTo.entrySet()) {
            // SQL Side
            try {
                // Put the sql entry
                this.putKeywordRefStmt.setString(1, entry.getValue().keyword);
                this.putKeywordRefStmt.setString(2, entry.getKey().keyword);
                this.putKeywordRefStmt.execute();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // Graph Side
            try (Session session = Graph.getInstance().getSession()) {
                session.writeTransaction(new TransactionWork<Void>() {
                    @Override
                    public Void execute(Transaction tx) {
                        Map<String, Object> params = new HashMap<>();
                        params.put("parent", entry.getValue().keyword);
                        params.put("child", entry.getKey().keyword);

                        // add intersecting papers weight
                        /**
                         * Since weights are collected at the end, both relation and node weights can be
                         * lists or numbers to be safe, convert both to lists and concatenate. the
                         * transaction query ran at the end will collect the weights
                         */
                        tx.run("MATCH (p:Topic { name: $parent })-[rel:RELATED_TO]-(c:Topic { name: $child }) "
                                + "WITH reduce(a = [], rW IN rel.weight | a + -rW) as listRelW, p "
                                + "WITH reduce(acc = [], pW IN p.weight | acc + pW) as pRelW, listRelW, p "
                                + "SET p.weight = pRelW + listRelW", params);

                        /**
                         * Merge the nodes while combining weight and edge references and dropping all
                         * names except for the parent's
                         */
                        tx.run("MATCH (n1:Topic { name: $parent }), (n2:Topic { name: $child }) "
                                + "WITH head(collect([n1, n2])) as nodes " + "CALL apoc.refactor.mergeNodes(nodes,{ "
                                + "  properties: { " + "      name:'discard', " + "      weight:'combine', "
                                + "      references:'combine' " + "    }, " + "   mergeRels:true " + "}) "
                                + "YIELD node " + "RETURN count(*) ", params);
                        return null;
                    }

                });
            }
        }
    }

    public void end() {
        try (Session session = Graph.getInstance().getSession()) {

            // cleanup the graph
            session.writeTransaction(new TransactionWork<Void>() {
                @Override
                public Void execute(Transaction tx) {
                    // remove self relationships
                    tx.run("MATCH (n:Topic)-[r:RELATED_TO]-(n:Topic) " + "DELETE r");

                    // add up relationship weights
                    tx.run("MATCH (:Topic)-[r:RELATED_TO]-(:Topic) " + "SET r.weight = size(r.references)");

                    // add up node weights
                    Result result = tx
                            .run("MATCH (n:Topic) " + "SET n.weight = reduce(base = 0, w IN n.weight | base + w) "
                                    + "WITH COLLECT(n) as nodes " + "RETURN nodes");

                    //add 
                    while (result.hasNext()) {
                        Map<String, Object> row = result.next().asMap();
                        for (Entry<String, Object> column : row.entrySet()) {
                            List<InternalNode> nodesList = (List<InternalNode>) column.getValue();
                            nodesList.forEach(nodeMap -> {
                                Map<String, Object> map = nodeMap.asMap();
                                String name = (String) map.get("name");
                                Long weight = (Long) map.get("weight");
                                try {
                                    updateWeightStmt.setLong(1, weight);
                                    updateWeightStmt.setString(2, name);
                                    updateWeightStmt.execute();
                                } catch (SQLException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                    return null;
                }
                
            });

            Graph.getInstance().close();
        }
    }
}
