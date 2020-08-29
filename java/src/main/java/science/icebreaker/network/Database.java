package science.icebreaker.network;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:postgresql://localhost:12210/icebreaker_network?user=postgres&password=postgres";
        return DriverManager.getConnection(url);
    }

}
