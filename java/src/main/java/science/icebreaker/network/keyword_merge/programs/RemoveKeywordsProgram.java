package science.icebreaker.network.keyword_merge.programs;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

import chen.chaoran.data_manager.core.Startable;
import science.icebreaker.network.Database;
import science.icebreaker.network.Graph;

public class RemoveKeywordsProgram implements Startable { 

    private Connection conn;
    private PreparedStatement removeKeywordStmt;
    
    public RemoveKeywordsProgram() throws SQLException {
        this.conn = Database.getConnection();
        this.removeKeywordStmt = conn.prepareStatement(
             "DELETE FROM keyword"
            +" WHERE name=? OR name=?;"
        );
    }

    private void deleteKeywords(String[] filePaths) throws SQLException {
        for(String fileDir : filePaths) {
            Path pathToFile = Paths.get(fileDir);
            try (BufferedReader br = Files.newBufferedReader(pathToFile)) {
                String line = br.readLine();
                while (line != null) {
                    String[] attributes = line.split(",");
                    if(attributes.length > 2 && attributes[2].equals("*")) {
                        String kw1s = attributes[0];
                        String kw2s = attributes[1];
                        this.removeKeywordStmt.setString(1, kw1s);
                        this.removeKeywordStmt.setString(2, kw2s);
                        this.removeKeywordStmt.execute();
                        try (Session session = Graph.getInstance().getSession()) {
                            session.writeTransaction(new TransactionWork<Void>() {
                                @Override
                                public Void execute(Transaction tx) {
                                    Map<String, Object> params = new HashMap<>();
                                    params.put("name1", kw1s);
                                    params.put("name2", kw2s);
                                    tx.run("MATCH (n1:Topic { name: $name1 }),(n2:Topic { name: $name2 }) "
                                    + "DETACH DELETE n1,n2; ", params);
                                    return null;
                                }
                            });
                        }
                    }

                    
                    line = br.readLine();
                }
                
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        Graph.getInstance().close();
    }

    @Override
    public void start(String[] args) throws Exception {
        this.deleteKeywords(args);
    }

}
