package science.icebreaker.network.wikidata;

import chen.chaoran.data_manager.core.ExhaustibleLinkedBlockingQueue;
import chen.chaoran.data_manager.core.JobManager;
import chen.chaoran.data_manager.core.Master;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.*;


public class WikidataImporterMaster implements Master<WikidataImporterJob> {

    @Override
    public WikidataImporterWorker createWorker() {
        return new WikidataImporterWorker();
    }

    @Override
    public void start(String[] args) throws Exception {
        if (args.length == 0 || args[0].equals("--help")) {
            System.out.println("WikidataImporter <path-to-wikidata-file (.bz2)>");
            System.exit(0);
        }

        ExhaustibleLinkedBlockingQueue<WikidataImporterJob> jobs = new ExhaustibleLinkedBlockingQueue<>(500);

        // Read the wikidata file
        Thread readerThread = new Thread(() -> {
            try {
                FileInputStream fin = new FileInputStream(args[0]);
                BufferedInputStream bis = new BufferedInputStream(fin);
                CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
                BufferedReader br2 = new BufferedReader(new InputStreamReader(input));
                String line;
                int i = 0;
                while ((line = br2.readLine()) != null) {
                    try {
                        i++;
                        if (!line.startsWith("{")) {
                            continue;
                        }
                        if (line.endsWith("\n")) {
                            line = line.substring(0, line.length() - 1);
                        }
                        if (line.endsWith(",")) {
                            line = line.substring(0, line.length() - 1);
                        }
                        jobs.put(new WikidataImporterJob(i, line));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (CompressorException | IOException e) {
                e.printStackTrace();
            }
            jobs.setExhausted(true);
        });
        readerThread.start();

        /* Executes the jobs */
        JobManager jobManager = new JobManager();
        jobManager.executeJobs(jobs, this);
    }
}
