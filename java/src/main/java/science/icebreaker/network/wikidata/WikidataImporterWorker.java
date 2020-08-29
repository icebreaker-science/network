package science.icebreaker.network.wikidata;

import chen.chaoran.data_manager.core.Worker;
import org.json.JSONObject;
import science.icebreaker.network.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;


public class WikidataImporterWorker implements Worker<WikidataImporterJob> {

    private final PreparedStatement statement;


    public WikidataImporterWorker() {
        try {
            Connection conn = Database.getConnection();
            statement = conn.prepareStatement(
                    "insert into wikidata(wikidata_id, data) values(?, ?::jsonb)"
            );
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }
    }

    @Override
    public void start(List<WikidataImporterJob> jobs) {
        for (WikidataImporterJob job : jobs) {
            try {
                String line = job.getLine();
                JSONObject json = new JSONObject(line);
                String wikidataId = json.getString("id");
                statement.setString(1, wikidataId);
                statement.setString(2, line);
                statement.execute();
            } catch (Exception e) {
                System.err.println("Error processing " + job.getJobId());
                e.printStackTrace();
            }
        }

    }
}
