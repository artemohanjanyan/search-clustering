package aohanjanyan.search_clustering.algorithm;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class BiGraphTest {
    @Test
    void testSortEdgesAndPart() {
        BiGraph biGraph = new BiGraph();
        biGraph.left.nodes.add(new BiGraph.Node() {
            {
                edges.add(new BiGraph.Edge(1, 1));
                edges.add(new BiGraph.Edge(2, 1));
                edges.add(new BiGraph.Edge(0, 1));
            }
        });
        biGraph.right.nodes.add(new BiGraph.Node() {
            {
                edges.add(new BiGraph.Edge(1, 1));
                edges.add(new BiGraph.Edge(1, 1));
                edges.add(new BiGraph.Edge(0, 1));
                edges.add(new BiGraph.Edge(2, 1));
                edges.add(new BiGraph.Edge(2, 0));
                edges.add(new BiGraph.Edge(2, 2));
            }
        });
        biGraph.sortEdges();
        assertThat(biGraph.left.nodes.get(0).edges,
                equalTo(Arrays.asList(
                        new BiGraph.Edge(0, 1),
                        new BiGraph.Edge(1, 1),
                        new BiGraph.Edge(2, 1))));
        assertThat(biGraph.right.nodes.get(0).edges,
                equalTo(Arrays.asList(
                        new BiGraph.Edge(0, 1),
                        new BiGraph.Edge(1, 1),
                        new BiGraph.Edge(1, 1),
                        new BiGraph.Edge(2, 0),
                        new BiGraph.Edge(2, 1),
                        new BiGraph.Edge(2, 2))));

        assertThat(biGraph.left.size(), equalTo(biGraph.left.nodes.size()));
        assertThat(biGraph.left.get(0), equalTo(biGraph.left.nodes.get(0)));
    }

    @Test
    void testEdgeEqualsAndHashCode() {
        Set<BiGraph.Edge> edges = new HashSet<>();
        edges.add(new BiGraph.Edge(0, 1));
        edges.add(new BiGraph.Edge(1, 2));
        edges.add(new BiGraph.Edge(1, 2));
        edges.add(new BiGraph.Edge(2, 3));
        assertThat(edges.size(), equalTo(3));
    }
}