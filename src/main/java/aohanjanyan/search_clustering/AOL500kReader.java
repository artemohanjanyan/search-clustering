package aohanjanyan.search_clustering;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Search log reader for "500k User Session Collection".
 * @see <a href="http://www.cim.mcgill.ca/~dudek/206/Logs/AOL-user-ct-collection">
 *     500k User Session Collection</a>
 */
public class AOL500kReader {
    private final SearchLog searchLog;
    private ReadObserver readObserver = null;

    /**
     * Constructs reader, which writes read data to provided search log.
     *
     * @param searchLog search log to write data to
     */
    public AOL500kReader(SearchLog searchLog) {
        this.searchLog = searchLog;
    }

    /**
     * Sets read observer.
     *
     * @param readObserver read observer
     * @see ReadObserver#lineRead(int)
     */
    public void setReadObserver(ReadObserver readObserver) {
        this.readObserver = readObserver;
    }

    /**
     * Reads the data.
     * @param reader reader to read data from
     * @throws IOException forwards exceptions from {@code reader}
     */
    public void readData(BufferedReader reader) throws IOException {
        skipHeaders(reader);

        String nextLine;
        int lineI = 0;
        while ((nextLine = reader.readLine()) != null) {
            String[] row = nextLine.split("\t");
            if (row.length != 5) {
                continue;
            }
            searchLog.addClickThrough(row[1], row[4]);

            if (readObserver != null) {
                readObserver.lineRead(++lineI);
            }
        }
    }

    private static void skipHeaders(BufferedReader reader) throws IOException {
        reader.readLine();
    }

    /**
     * Observes read operations of {@link AOL500kReader}.
     */
    public interface ReadObserver {
        /**
         * Called after reading of each line from the input.
         * @param lineI read line count
         */
        void lineRead(int lineI);
    }
}
