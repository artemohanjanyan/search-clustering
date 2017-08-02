package aohanjanyan.search_clustering;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class BiGraph {

    public Part left = new Part(), right = new Part();

    private static void sortPartEdges(Part part) {
        for (Node node : part.nodes) {
            node.edges.sort(null);
        }
    }

    public void sortEdges() {
        sortPartEdges(left);
        sortPartEdges(right);
    }

    public static class Edge implements Comparable<Edge> {
        public int dst;
        public int weight;

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

        @Override
        public int compareTo(Edge that) {
            if (this.dst < that.dst) {
                return -1;
            } else if (this.dst > that.dst) {
                return 1;
            }

            if (this.weight < that.weight) {
                return -1;
            } else if (this.weight > that.weight) {
                return 1;
            }
            return 0;
        }
    }

    public static class Node {
        List<Edge> edges = new ArrayList<>();
    }

    public static class Part {
        List<Node> nodes = new ArrayList<>();

        public int size() {
            return nodes.size();
        }

        public Node get(int i) {
            return nodes.get(i);
        }
    }
}
