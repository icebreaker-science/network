package science.icebreaker.network;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

public class Graph {
    private static Graph database;
    private Driver driver;

    private Graph() {
        final String URI = "neo4j://localhost:7687";
        final String username = "neo4j";
        final String password = "neo4j";
        this.driver = GraphDatabase.driver(URI, AuthTokens.basic(username, password));
    }

    public static Graph getInstance() {
        if(database == null)
            database = new Graph();
        
        return database;
    }

    public Session getSession() {
        return driver.session();
    }

	public void close() {
        this.driver.close();
	}
}
