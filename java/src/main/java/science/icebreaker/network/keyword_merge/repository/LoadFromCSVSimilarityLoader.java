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
                    boolean isSimilar = attributes.length > 2 && !attributes[2].equals("*");
                    if(!isSimilar) {
                        line = br.readLine();
                        continue;
                    }

                    String kw1s = attributes[0];
                    String kw2s = attributes[1];
                    String origins = attributes[2];

                    if(kw1s.endsWith("*")) kw1s = kw1s.substring(0, kw1s.length() - 1);
                    if(kw2s.endsWith("*")) kw2s = kw2s.substring(0, kw2s.length() - 1);

                    Keyword kw1 = mergeKeyword(kw1s);
                    Keyword kw2 = mergeKeyword(kw2s);
                    Keyword origin = mergeKeyword(origins);
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
