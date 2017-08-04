package aohanjanyan.search_clustering.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a weighted bipartite graph as an adjacency list.
 */
public class BiGraph {

    /**
     * Variable holding corresponding part of BiGraph.
     */
    public Part left = new Part(), right = new Part();

    /**
     * Sort all edges in adjacency list according to their destination node numbers.
     */
    public void sortEdges() {
        left.sortEdges();
        right.sortEdges();
    }

    /**
     * Represents a directed edge.
     * Holds destination node number and weight of an edge.
     */
    public static class Edge {
        public int dst;
        public int weight;

        /**
         * Constructs an edge.
         *
         * @param dst    index of destination node
         * @param weight weight of this edge
         */
        public Edge(int dst, int weight) {
            this.dst = dst;
            this.weight = weight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Edge edge = (Edge) o;

            return dst == edge.dst && weight == edge.weight;
        }

        @Override
        public int hashCode() {
            int result = dst;
            result = 31 * result + weight;
            return result;
        }
    }

    /**
     * Represents a node, which is just a list of outgoing edges.
     */
    public static class Node {
        /**
         * A list of outgoing edges.
         */
        public List<Edge> edges = new ArrayList<>();

        /**
         * Sort all edges according to their destination node numbers.
         */
        public void sortEdges() {
            edges.sort((edge1, edge2) -> {
                if (edge1.dst < edge2.dst) {
                    return -1;
                } else if (edge1.dst > edge2.dst) {
                    return 1;
                }

                if (edge1.weight < edge2.weight) {
                    return -1;
                } else if (edge1.weight > edge2.weight) {
                    return 1;
                }

                return 0;
            });
        }
    }

    /**
     * Represents a part of the graph, which is just a list of nodes.
     */
    public static class Part {
        /**
         * A list of nodes.
         */
        public List<Node> nodes = new ArrayList<>();

        /**
         * Returns a number of nodes in a part.
         *
         * @return a number of nodes in a part
         */
        public int size() {
            return nodes.size();
        }

        /**
         * Returns a node at specified index.
         * @param i index of a node
         * @return a node at specified index
         */
        public Node get(int i) {
            return nodes.get(i);
        }

        /**
         * Sort all edges according to their destination node numbers.
         */
        public void sortEdges() {
            for (Node node : nodes) {
                node.sortEdges();
            }
        }
    }
}
