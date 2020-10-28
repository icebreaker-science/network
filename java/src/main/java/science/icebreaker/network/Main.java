package science.icebreaker.network;

import chen.chaoran.data_manager.core.Startable;
import science.icebreaker.network.keyword_merge.programs.BaseKeywordMergeProgram;
import science.icebreaker.network.keyword_merge.programs.MergeResultsInDBProgram;
import science.icebreaker.network.wikidata.WikidataImporterMaster;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {


    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Please specify a program.");
            System.exit(1);
        }

        Map<String, Class<? extends Startable>> availablePrograms = new HashMap<String, Class<? extends Startable>>() {
            {
                put("WikidataImporter", WikidataImporterMaster.class);
                put("KeywordMerge", BaseKeywordMergeProgram.class);
                put("MergeInDB", MergeResultsInDBProgram.class);
            }
        };

        long started = System.currentTimeMillis();
        System.out.println("Started at: " + LocalDateTime.now());
        System.out.println("Program path: " + Paths.get(".").toAbsolutePath());

        String chosenProgramName = args[0];
        String[] argsForProgram = Arrays.copyOfRange(args, 1, args.length);

        Class<? extends Startable> c = availablePrograms.get(chosenProgramName);
        if (c == null) {
            System.out.println("Unknown program.");
            System.exit(1);
        }
        Startable startable = c.getConstructor().newInstance();

        try {
            startable.start(argsForProgram);
        } catch (Exception e) {
            System.err.println("Finished with error at: " + LocalDateTime.now());
            System.err.println("Total time: " + (System.currentTimeMillis() - started) + "ms");
            throw e;
        }

        System.out.println("Finished with no error at: " + LocalDateTime.now());
        System.out.println("Total time: " + (System.currentTimeMillis() - started) + "ms");
    }

}
