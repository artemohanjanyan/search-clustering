package aohanjanyan.search_clustering;

import aohanjanyan.search_clustering.algorithm.Clustering;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Main class for clustering algorithm.
 */
public class Main {

    private static class Options {
        @Option(name = "-threshold", usage = "threshold")
        double threshold = 0.1;

        @Option(name = "-o", usage = "output file", required = true)
        File output;

        @Argument(metaVar = "arguments", multiValued = true, usage = "input files", required = true)
        List<File> inputFiles;
    }

    /**
     * Entry point for clustering algorithm.
     * Reads data from input files and prints the result to the output file.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        Options options = new Options();
        CmdLineParser cmdLineParser = new CmdLineParser(options);

        try {
            cmdLineParser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            cmdLineParser.printUsage(System.err);
            return;
        }

        PrintWriter outputPrinter;
        try {
            outputPrinter = new PrintWriter(Files.newBufferedWriter(
                    options.output.toPath(),
                    StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Can't open file " + options.output.toString());
            System.err.println("Stacktrace:");
            e.printStackTrace();
            return;
        }

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

        for (File inputFile : options.inputFiles) {
            try (BufferedReader reader =
                         Files.newBufferedReader(inputFile.toPath(), StandardCharsets.UTF_8)) {
                new BufferedReader(reader, 2);
                System.out.println("Reading " + inputFile + "...");
                aol500kReader.readData(reader);
                System.out.println("\nFinished reading " + inputFile + "\n");
            } catch (IOException e) {
                System.err.println("Can't open file " + inputFile);
                System.err.println("Stacktrace:");
                e.printStackTrace();
                return;
            }
        }
        searchLog.queries = null;
        searchLog.clicks = null;
        searchLog.clickNames = null;

        searchLog.graph.sortEdges();
        System.out.println(searchLog.graph.left.nodes.size() + " unique queries");
        System.out.println(searchLog.graph.right.nodes.size() + " unique clicks");
        System.out.println();

        System.out.println("Running clustering...");
        Clustering clustering = new Clustering(options.threshold);
        clustering.setClusteringObserver(new Clustering.ClusteringObserver() {
            @Override
            public void nodeInitFinished(int nodeI) {
                if (nodeI % 1000 == 0) {
                    System.out.print(nodeI + " ");
                }
                if (nodeI % 10000 == 0) {
                    System.out.println();
                }
            }

            @Override
            public void initFinished() {
                System.out.print("\n\nInit finished\n\n");
            }

            @Override
            public void nodesMerged(int i, int j, boolean isLeftPart, int mergeI) {
                if (mergeI % 100 == 0) {
                    System.out.print(mergeI + " ");
                }
                if (mergeI % 1000 == 0) {
                    System.out.println();
                }
            }
        });
        clustering.cluster(searchLog.graph);
        System.out.println("Finished");

        System.out.println("\nPrinting results to output file...");

        for (int i = 0; i < searchLog.queryNames.size(); i++) {
            outputPrinter.print(clustering.getLeftSets().find(i));
            outputPrinter.print('\t');
            outputPrinter.println(searchLog.queryNames.get(i));
        }
        outputPrinter.close();
    }
}
