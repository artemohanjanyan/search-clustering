package aohanjanyan.search_clustering;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class BiGraph {

    public static class Node {
        List<Edge> edges = new ArrayList<>();
    }

    public static class Edge {
        int dst;
        int weight;

        public Edge(int dst, int weight) {
            this.dst = dst;
            this.weight = weight;
        }
    }

    public List<Node> left = new ArrayList<>(), right = new ArrayList<>();
}
