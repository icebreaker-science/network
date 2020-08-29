package chen.chaoran.data_manager.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;


/**
 * Given a (select) sql query, this class can return a BlockingQueue that is filled with the data from the database.
 * Assuming that the result set contains sufficient many rows, the data will not be filled immediately but fetched
 * dynamically from the database. In order to do that, this class will start a thread that will insert values into the
 * queue as long as some are available.
 */
public class DatabaseReaderQueueBuilder<T extends Job> {

    private PreparedStatement statement;
    private Function<ResultSet, T> rowToJobFunction;
    private int capacity;
    private int fetchSize;

    /**
     * @param statement - The connection of "statement" must not be used by any other object.
     * @param rowToJobFunction - "rowToJobFunction" must not call next() on the result set or manipulate it in any other
     *                         way.
     */
    public DatabaseReaderQueueBuilder(PreparedStatement statement, Function<ResultSet, T> rowToJobFunction) {
        this.statement = statement;
        this.rowToJobFunction = rowToJobFunction;
        this.capacity = 5000;
        this.fetchSize = 200;
    }


    /**
     * Sets the maximal capacity of the queue.
     */
    public DatabaseReaderQueueBuilder setQueueCapacity(int capacity) {
        this.capacity = capacity;
        return this;
    }


    /**
     * Please see java.sql.Statement.setFetchSize()
     */
    public DatabaseReaderQueueBuilder setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
        return this;
    }


    public BlockingQueue<T> build() throws SQLException {
        ExhaustibleBlockingQueue<T> queue = new ExhaustibleLinkedBlockingQueue<>(this.capacity);
        Connection conn = this.statement.getConnection();
        conn.setAutoCommit(false);
        statement.setFetchSize(this.fetchSize);
        Thread fetcherThread = new Thread(() -> {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    T job = this.rowToJobFunction.apply(rs);
                    queue.put(job);
                }
                queue.setExhausted(true);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (InterruptedException ignored) { }
        });
        fetcherThread.start();
        return queue;
    }
}
