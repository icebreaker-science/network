package science.icebreaker.network.keyword_merge.resolvers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;
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
	private Set<String> usedKeywords;

    public MergeInDBResolver(KeywordRepository keywordRepo) {
        this.keywordRepo = keywordRepo;
        this.usedKeywords = new HashSet<String>();
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
        this.usedKeywords.add(word1.keyword);
        this.usedKeywords.add(word2.keyword);
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

    public void propagateResultsToDB(Set<Keyword> newKeywords) throws Exception {
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

        // When resuming a merge process, use this, as new keywords have been
        // already added in sql but not in neo4j

        // this.usedKeywords.forEach(nkw -> {
        //     try (Session session = Graph.getInstance().getSession()) {
        //         session.writeTransaction(new TransactionWork<Void>() {
        //             @Override
        //             public Void execute(Transaction tx) {
        //                 Map<String, Object> params = new HashMap<>();
        //                 params.put("name", nkw);
        //                 tx.run("CREATE (:Topic {name:$name, weight:0})", params);
        //                 return null;
        //             }
        //         });
        //     }
        // });

        int total = this.refersTo.size();
        int current = 0;
        int countEach = 100;
        for (Entry<Keyword, Keyword> entry : this.refersTo.entrySet()) {
            // Skip self reference
            if(entry.getKey().keyword.equals(entry.getValue().keyword))
                continue;

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

                        /**
                         * New node weight is equal both node weight - the weight of the edge between them (the common references)
                         * Adding both nodes' weight is done after merging
                         */
                        tx.run("MATCH (p:Topic { name: $parent })-[rel:RELATED_TO]-(c:Topic { name: $child }) "
                                + "WITH p, p.weight - rel.weight as nW "
                                + "SET p.weight = nW", params);
                        /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
                         * Merge the nodes while combining weight and edge references and dropping all
                         * names except for the parent's
                        */
                        tx.run("MATCH (n1:Topic { name: $parent }), (n2:Topic { name: $child }) "
                                + "WITH head(collect([n1, n2])) as nodes "
                                + "CALL apoc.refactor.mergeNodes(nodes,{ "
                                + "  properties: { "
                                + "      name:'discard', "
                                + "      weight:'combine', "
                                + "      references:'combine' "
                                + "    }, "
                                + "   mergeRels:true "
                                + "}) "
                                + "YIELD node " 
                                + "RETURN count(*) ", params);

                        
                        // remove self relationships
                        tx.run("MATCH (n:Topic {name: $parent})-[r:RELATED_TO]-(n) DELETE r", params);

                        /**
                         * 1-Since merging does not recalculate the edge weights, do it manually
                         * 2-Node weights are combined in a list by the previous function, add them.
                         */
                        tx.run("MATCH(n:Topic {name: $parent})-[rel:RELATED_TO]-(n2:Topic) "
                                + "WITH n, rel, size(rel.references) as relW, reduce(acc=0, varW IN n.weight | acc + varW) as cW "
                                + "SET rel.weight = relW "
                                + "SET n.weight = cW ", params);

                        return null;
                    }

                });
            }
            
            try (Session session = Graph.getInstance().getSession()) {
                session.writeTransaction(new TransactionWork<Void>() {
                    @Override
                    public Void execute(Transaction tx) {
                        Map<String, Object> params = new HashMap<>();
                        params.put("parent", entry.getValue().keyword);

                        /**
                         * Set the weights in postgres to the new values
                         */
                        Result res = tx.run("MATCH(n:Topic {name: $parent}) RETURN n", params);
                        res.forEachRemaining(record -> {
                            int newWeight = Integer.parseInt(record.get("n").asMap().get("weight").toString());
                            try {
                                updateWeightStmt.setLong(1, newWeight);
                                updateWeightStmt.setString(2, entry.getValue().keyword);
                                updateWeightStmt.execute();
                            } catch (SQLException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        });
                        
                        return null;
                    }
                });
            }

            current++;
            if(current % countEach == 0)
                System.out.println("Merged: " + current + "/" + total);
        }
        System.out.println("Done merging");
    }

    public void end() {
        Graph.getInstance().close();
    }

    public void findCycles() {
        Set<Keyword> toDisconnect = new HashSet<Keyword>();
        for(Entry<Keyword, Keyword> keywordEntry : this.refersTo.entrySet()) {
            if(keywordEntry.getKey() == keywordEntry.getValue())
                toDisconnect.add(keywordEntry.getKey());
        }

        for(Keyword keyword : toDisconnect) {
            this.refersTo.remove(keyword);
        }

        for(Keyword keyword : this.refersTo.keySet()) {
            findCycle(keyword, new HashSet<Keyword>());
        }
    }
    
    private void findCycle(Keyword word, Set<Keyword> set) {
        Keyword next = this.refersTo.getOrDefault(word, word);
        if (next.equals(word))
            return;
        else if(set.contains(next)) {
            handleCycles(set);
        }
        else {
            set.add(word);
            set.add(next);
            findCycle(next, set);
        }
    }

    private void handleCycles(Set<Keyword> set) {
        String chain = set.stream().map(keyword -> keyword.keyword).reduce("", (acc, val) -> acc+","+val);
        System.out.println(chain);
    }


    /**
     * Adds missing keywords from postgres to neo4j
     */
    public void syncKeywords() {
        final Set<String> neo4jKeywords = new HashSet<String>();

        try (Session session = Graph.getInstance().getSession()) {
            session.writeTransaction(new TransactionWork<Void>() {
                @Override
                public Void execute(Transaction tx) {
                    Result res = tx.run("MATCH (n:Topic) RETURN n");
                    res.forEachRemaining(record -> neo4jKeywords.add(record.get("n").asMap().get("name").toString()));
                    return null;
                }
           });
        }

        this.usedKeywords.removeAll(neo4jKeywords);
    }
}
