package aohanjanyan.search_clustering;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * TODO
 */
public class AOL500kReader {
    private final SearchLog searchLog;
    private ReadObserver readObserver = null;

    public AOL500kReader(SearchLog searchLog) {
        this.searchLog = searchLog;
    }

    private static void skipHeaders(BufferedReader reader) throws IOException {
        reader.readLine();
    }

    public void setReadObserver(ReadObserver readObserver) {
        this.readObserver = readObserver;
    }

    public void readData(BufferedReader reader) throws IOException {
        skipHeaders(reader);

        String nextLine;
        int lineI = 0;
        while ((nextLine = reader.readLine()) != null) {
            final String[] row = nextLine.split("\t");
            if (row.length != 5) {
                continue;
            }
            searchLog.addClickThrough(row[1], row[4]);

            readObserver.lineRead(++lineI);
        }
    }

    public interface ReadObserver {
        void lineRead(int lineI);
    }
}
