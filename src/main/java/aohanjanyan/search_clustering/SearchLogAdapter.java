package aohanjanyan.search_clustering;

import java.util.List;
import java.util.Map;

public class SearchLogAdapter {
    private final BiGraph biGraph;
    private final SearchLogInfo searchLogInfo;

    public SearchLogAdapter(BiGraph biGraph, SearchLogInfo searchLogInfo) {
        this.biGraph = biGraph;
        this.searchLogInfo = searchLogInfo;
    }

    private static int getNodeI(List<BiGraph.Node> graphPart,
                                Map<String, Integer> indexMap,
                                String str) {
        if (!indexMap.containsKey(str)) {
            indexMap.put(str, indexMap.size());
            graphPart.add(new BiGraph.Node());
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
        int queryI = getNodeI(biGraph.left, searchLogInfo.queries, query);
        int clickI = getNodeI(biGraph.right, searchLogInfo.clicks, click);

        addEdge(biGraph.left.get(queryI), clickI);
        addEdge(biGraph.right.get(clickI), queryI);
    }
}
