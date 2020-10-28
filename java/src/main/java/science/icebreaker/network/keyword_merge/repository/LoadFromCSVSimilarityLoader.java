package science.icebreaker.network.keyword_merge.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import science.icebreaker.network.keyword_merge.comparators.SimilarityResult;
import science.icebreaker.network.keyword_merge.entities.Keyword;

public class LoadFromCSVSimilarityLoader extends SimilarityResultLoader {
    private List<String> fileDirs;

    public LoadFromCSVSimilarityLoader(List<String> fileDirs, KeywordRepository keywordRepo) {
        super(keywordRepo);
        this.fileDirs = fileDirs;
    }

    @Override
    public void load() {
        for(String fileDir : this.fileDirs) {
            Path pathToFile = Paths.get(fileDir);
            try (BufferedReader br = Files.newBufferedReader(pathToFile)) {
                String line = br.readLine();
                while (line != null) {

                    String[] attributes = line.split(",");
                    //KW1, KW2, Suggested Sim Word, Merge Decision, Keyword To Merge
                    Keyword kw1 = mergeKeyword(attributes[0]);
                    Keyword kw2 = mergeKeyword(attributes[1]);
                    Keyword origin = mergeKeyword(attributes[4]);
                    boolean isSimilar = attributes[3].equals("true");
                    SimilarityResult result = new SimilarityResult(kw1, kw2, origin, isSimilar);
                    this.similarity.add(result);
                    line = br.readLine();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
    
}
