package science.icebreaker.network.wikidata;

import chen.chaoran.data_manager.core.Job;


public class WikidataImporterJob implements Job {

    private final int lineIndex;

    private final String line;


    public WikidataImporterJob(int lineIndex, String line) {
        this.lineIndex = lineIndex;
        this.line = line;
    }


    @Override
    public String getJobId() {
        return "Line " + lineIndex;
    }


    public String getLine() {
        return line;
    }
}
