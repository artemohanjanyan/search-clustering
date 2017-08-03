package aohanjanyan.search_clustering;

import aohanjanyan.search_clustering.algorithm.BiGraph;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 */
public class SearchLog {
    public BiGraph graph = new BiGraph();
    public Map<String, Integer> queries = new HashMap<>();
    public Map<String, Integer> clicks = new HashMap<>();

    private static int getNodeI(BiGraph.Part graphPart,
                                Map<String, Integer> indexMap,
                                String str) {
        if (!indexMap.containsKey(str)) {
            indexMap.put(str, indexMap.size());
            graphPart.nodes.add(new BiGraph.Node());
        }
        return indexMap.get(str);
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

    public void addClickThrough(String query, String click) {
        int queryI = getNodeI(graph.left, queries, query);
        int clickI = getNodeI(graph.right, clicks, click);

        addEdge(graph.left.nodes.get(queryI), clickI);
        addEdge(graph.right.nodes.get(clickI), queryI);
    }
}
