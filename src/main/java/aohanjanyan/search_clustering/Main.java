package aohanjanyan.search_clustering;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * TODO
 */
public class Main {
    public static void main(String[] args) {
        SearchLog searchLog = new SearchLog();
        AOL500kReader aol500kReader = new AOL500kReader(searchLog);

        aol500kReader.setReadObserver(lineI -> {
            if (lineI % 100000 == 0) {
                System.out.print(lineI + " ");
            }
            if (lineI % 1000000 == 0) {
                System.out.println();
            }
        });

        for (String filePath : args) {
            try (final BufferedReader reader =
                         Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
                new BufferedReader(reader, 2);
                System.out.println("Reading " + filePath + "...");
                aol500kReader.readData(reader);
                System.out.println("\nFinished reading " + filePath + "\n");
            } catch (IOException e) {
                System.err.println("Can't open file " + filePath);
                System.err.println("Stacktrace:");
                e.printStackTrace();
                return;
            }
        }

        System.out.println(searchLog.graph.left.size() + " unique queries");
        System.out.println(searchLog.graph.right.size() + " unique clicks");
    }
}
