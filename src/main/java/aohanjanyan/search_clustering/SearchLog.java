package aohanjanyan.search_clustering;

import aohanjanyan.search_clustering.algorithm.BiGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for storing information about search log.
 */
public class SearchLog {
    /**
     * BiGraph constructed from clickthroughs
     */
    public BiGraph graph = new BiGraph();
    /**
     * Map from query to their indexes.
     */
    public Map<String, Integer> queries = new HashMap<>();
    /**
     * Map from click URLs to their indexes.
     */
    public Map<String, Integer> clicks = new HashMap<>();
    /**
     * List of queries. Used as map from query indexes to queries.
     */
    public List<String> queryNames = new ArrayList<>();
    /**
     * List of click URLs. Used as map from click indexes to click URLs.
     */
    public List<String> clickNames = new ArrayList<>();

    private static int getNodeI(BiGraph.Part part,
                                Map<String, Integer> indexMap,
                                List<String> nameList,
                                String name) {
        if (!indexMap.containsKey(name)) {
            part.nodes.add(new BiGraph.Node());
            indexMap.put(name, indexMap.size());
            nameList.add(name);
        }
        return indexMap.get(name);
    }

    private static void addEdge(BiGraph.Node node, int otherNodeI) {
        for (BiGraph.Edge edge : node.edges) {
            if (edge.dst == otherNodeI) {
                ++edge.weight;
                return;
            }
        }
        node.edges.add(new BiGraph.Edge(otherNodeI, 1));
    }

    /**
     * Adds clickthrough to search log.
     *
     * @param query query of the clickthrough
     * @param click click URL of the clickthrough
     */
    public void addClickThrough(String query, String click) {
        int queryI = getNodeI(graph.left, queries, queryNames, query);
        int clickI = getNodeI(graph.right, clicks, clickNames, click);

        addEdge(graph.left.nodes.get(queryI), clickI);
        addEdge(graph.right.nodes.get(clickI), queryI);
    }
}
