package science.icebreaker.network.keyword_merge.resolvers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import science.icebreaker.network.keyword_merge.comparators.SimilarityResult;

public class WriteToFileResolver implements SimilarityResultResolver {

    private final BufferedWriter BW;
    private final Integer flushAfter;
    private int counter;
    private int total;

    public WriteToFileResolver(String path, Integer flushAfter) throws IOException {
        this.BW = new BufferedWriter(new FileWriter(path));
        this.flushAfter = flushAfter;
    }

    @Override
    public void merge(SimilarityResult result) {
        try {
            this.BW.write(result.kw1.keyword + "," + result.kw2.keyword + "," + result.origin.keyword + "\n");
            counter++;

            // Report results once in a while
            if(counter == this.flushAfter) {
                total += counter;
                System.out.println("Total Resolved: " + total);
                this.counter = 0;
                this.BW.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void end() {
        try {
            this.BW.flush();
            this.BW.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
