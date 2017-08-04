package aohanjanyan.search_clustering.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ClusteringTest {
    private BiGraph graph;
    private Clustering.GraphPartData leftPartData;
    private Clustering.GraphPartData rightPartData;
    private Clustering clustering;

    @BeforeEach
    void initBiGraph() {
        clustering = new Clustering(0.01);

        graph = new BiGraph();
        // Graph 1.b) from the article
        graph.left.nodes.add(new BiGraph.Node() {
            {
                this.edges.add(new BiGraph.Edge(0, 1000));
            }
        });
        graph.left.nodes.add(new BiGraph.Node() {
            {
                this.edges.add(new BiGraph.Edge(0, 10));
                this.edges.add(new BiGraph.Edge(1, 1000));
            }
        });
        graph.left.nodes.add(new BiGraph.Node() {
            {
                this.edges.add(new BiGraph.Edge(1, 1000));
            }
        });
        graph.right.nodes.add(new BiGraph.Node() {
            {
                this.edges.add(new BiGraph.Edge(0, 1000));
                this.edges.add(new BiGraph.Edge(1, 10));
            }
        });
        graph.right.nodes.add(new BiGraph.Node() {
            {
                this.edges.add(new BiGraph.Edge(1, 1000));
                this.edges.add(new BiGraph.Edge(2, 1000));
            }
        });
        graph.sortEdges();

        leftPartData = clustering.initGraphPartData(graph.left, graph.right);
        rightPartData = clustering.initGraphPartData(graph.right, graph.left);
    }

    @Test
    void testCalculateSimilarity() {
        double similarity = Clustering.calculateSimilarity(
                graph.right.get(0),
                graph.right.get(1));
        assertThat(similarity, closeTo(1010.0 / 3010.0, 1e-5));
    }

    @Test
    void testInsertSimilarity() {
        graph = new BiGraph();
        graph.left.nodes.add(new BiGraph.Node() {
            {
                this.edges.add(new BiGraph.Edge(0, 1));
            }
        });
        graph.left.nodes.add(new BiGraph.Node() {
            {
                this.edges.add(new BiGraph.Edge(0, 1));
                this.edges.add(new BiGraph.Edge(1, 1000));
            }
        });
        graph.right.nodes.add(new BiGraph.Node() {
            {
                this.edges.add(new BiGraph.Edge(0, 1));
                this.edges.add(new BiGraph.Edge(1, 1));
            }
        });
        graph.right.nodes.add(new BiGraph.Node() {
            {
                this.edges.add(new BiGraph.Edge(1, 1000));
            }
        });
        graph.sortEdges();

        leftPartData = clustering.initGraphPartData(graph.left, graph.right);
        rightPartData = clustering.initGraphPartData(graph.right, graph.left);

        clustering.insertSimilarity(graph.left, leftPartData, 0, 1);

        assertThat(leftPartData.similarities, empty());
    }

    @Test
    void testSiblings() {
        assertThat(Clustering.siblings(graph.left, graph.right, 0),
                contains(1));
        assertThat(Clustering.siblings(graph.left, graph.right, 1),
                containsInAnyOrder(0, 2));
        assertThat(Clustering.siblings(graph.left, graph.right, 2),
                contains(1));
        assertThat(Clustering.siblings(graph.right, graph.left, 0),
                contains(1));
        assertThat(Clustering.siblings(graph.right, graph.left, 1),
                contains(0));
    }

    @Test
    void testInitGraphPartData() {
        assertThat(leftPartData.nodesSimilarities.get(0),
                containsInAnyOrder(
                        new Clustering.Similarity(1010.0 / 2010.0, 0, 1)
                ));
        assertThat(leftPartData.nodesSimilarities.get(1),
                containsInAnyOrder(
                        new Clustering.Similarity(1010.0 / 2010.0, 0, 1),
                        new Clustering.Similarity(2000.0 / 2010.0, 1, 2)
                ));
        assertThat(leftPartData.nodesSimilarities.get(2),
                containsInAnyOrder(
                        new Clustering.Similarity(2000.0 / 2010.0, 1, 2)
                ));
        assertThat(leftPartData.similarities,
                containsInAnyOrder(
                        new Clustering.Similarity(1010.0 / 2010.0, 0, 1),
                        new Clustering.Similarity(2000.0 / 2010.0, 1, 2)
                ));

        assertThat(rightPartData.nodesSimilarities.get(0),
                containsInAnyOrder(
                        new Clustering.Similarity(1010.0 / 3010.0, 0, 1)
                ));
        assertThat(rightPartData.nodesSimilarities.get(0),
                containsInAnyOrder(
                        new Clustering.Similarity(1010.0 / 3010.0, 0, 1)
                ));
        assertThat(rightPartData.similarities,
                containsInAnyOrder(
                        new Clustering.Similarity(1010.0 / 3010.0, 0, 1)
                ));
    }

    @Test
    void testRemoveSimilaritiesWith() {
        Clustering.removeSimilaritiesWith(leftPartData, 0);
        assertThat(leftPartData.nodesSimilarities.get(0), empty());
        assertThat(leftPartData.nodesSimilarities.get(1),
                containsInAnyOrder(
                        new Clustering.Similarity(2000.0 / 2010.0, 1, 2)
                ));
        assertThat(leftPartData.nodesSimilarities.get(2),
                containsInAnyOrder(
                        new Clustering.Similarity(2000.0 / 2010.0, 1, 2)
                ));
        assertThat(leftPartData.similarities,
                containsInAnyOrder(
                        new Clustering.Similarity(2000.0 / 2010.0, 1, 2)
                ));

        Clustering.removeSimilaritiesWith(rightPartData, 0);
        assertThat(rightPartData.nodesSimilarities.get(0), empty());
        assertThat(rightPartData.nodesSimilarities.get(1), empty());
        assertThat(rightPartData.similarities, empty());
    }

    @Test
    void testMergeNodes1() {
        clustering.mergeNodes(graph.right, rightPartData, graph.left, leftPartData,
                rightPartData.similarities.descendingIterator().next());

        int rightNodeI = rightPartData.clusterSets.find(0);

        assertThat(graph.left.nodes.get(0).edges, contains(new BiGraph.Edge(rightNodeI, 1000)));
        assertThat(graph.left.nodes.get(1).edges, contains(new BiGraph.Edge(rightNodeI, 1010)));
        assertThat(graph.left.nodes.get(2).edges, contains(new BiGraph.Edge(rightNodeI, 1000)));

        assertThat(graph.right.nodes.get(rightNodeI).edges, contains(
                new BiGraph.Edge(0, 1000),
                new BiGraph.Edge(1, 1010),
                new BiGraph.Edge(2, 1000)
        ));

        assertThat(leftPartData.similarities, hasSize(3));
        assertThat(rightPartData.similarities, hasSize(0));
    }

    @Test
    void testMergeNodes2() {
        clustering.mergeNodes(graph.right, rightPartData, graph.left, leftPartData,
                rightPartData.similarities.descendingIterator().next());
        int rightNodeI = rightPartData.clusterSets.find(0);

        clustering.mergeNodes(graph.left, leftPartData, graph.right, rightPartData,
                leftPartData.nodesSimilarities.get(2).iterator().next());
        int leftNodeI = leftPartData.clusterSets.find(2);

        assertThat(graph.left.nodes.get(0).edges,
                contains(new BiGraph.Edge(rightNodeI, 1000)));
        assertThat(graph.left.nodes.get(leftNodeI).edges,
                contains(new BiGraph.Edge(rightNodeI, 2010)));

        assertThat(graph.right.nodes.get(rightNodeI).edges, contains(
                new BiGraph.Edge(0, 1000),
                new BiGraph.Edge(leftNodeI, 2010)
        ));

        assertThat(leftPartData.similarities, hasSize(1));
        assertThat(rightPartData.similarities, hasSize(0));
    }

    @Test
    void testMergeNodes3() {
        clustering.mergeNodes(graph.left, leftPartData, graph.right, rightPartData,
                leftPartData.similarities.descendingIterator().next());
        int newNodeI = leftPartData.clusterSets.find(1);
        assertThat(graph.left.nodes.get(0).edges, contains(new BiGraph.Edge(0, 1000)));
        assertThat(graph.left.nodes.get(newNodeI).edges, contains(
                new BiGraph.Edge(0, 10),
                new BiGraph.Edge(1, 2000)
        ));
        assertThat(graph.right.nodes.get(0).edges, contains(
                new BiGraph.Edge(0, 1000),
                new BiGraph.Edge(newNodeI, 10)
        ));
        assertThat(graph.right.nodes.get(1).edges, contains(
                new BiGraph.Edge(newNodeI, 2000)
        ));
        assertThat(leftPartData.similarities, hasSize(1));
        assertThat(rightPartData.similarities, hasSize(1));
    }

    @Test
    void testFindMaxSimilarity() {
        assertThat(clustering.findMaxSimilarity(leftPartData),
                equalTo(new Clustering.Similarity(2000.0 / 2010.0, 1, 2)));
        assertThat(clustering.findMaxSimilarity(rightPartData),
                equalTo(new Clustering.Similarity(1010.0 / 3010.0, 0, 1)));
    }

    @Test
    void testCluster() {
        final AtomicInteger initCount = new AtomicInteger(0);
        final AtomicInteger initFinishCount = new AtomicInteger(0);
        final AtomicInteger mergeCount = new AtomicInteger(0);
        clustering.setClusteringObserver(new Clustering.ClusteringObserver() {
            @Override
            public void nodeInitFinished(int nodeI) {
                initCount.addAndGet(1);
            }

            @Override
            public void initFinished() {
                initFinishCount.addAndGet(1);
            }

            @Override
            public void nodesMerged(int i, int j, boolean isLeftPart, int mergeI) {
                mergeCount.addAndGet(1);
            }
        });

        clustering.cluster(graph);

        assertThat(initCount.get(), is(5));
        assertThat(initFinishCount.get(), is(1));
        assertThat(mergeCount.get(), is(3));

        assertThat(clustering.getLeftSets().find(0), allOf(
                equalTo(clustering.getLeftSets().find(1)),
                equalTo(clustering.getLeftSets().find(2))
        ));
        assertThat(clustering.getRightSets().find(0),
                equalTo(clustering.getRightSets().find(1)));
    }

    @Test
    void testSimilarityCompareTo() {
        assertThat(new Clustering.Similarity(0, 1, 2),
                lessThan(new Clustering.Similarity(2, 1, 0)));

        assertThat(new Clustering.Similarity(0.5, 1, 2),
                greaterThan(new Clustering.Similarity(0, 1, 2)));

        assertThat(new Clustering.Similarity(0.5, 1, 2),
                lessThan(new Clustering.Similarity(0.5, 2, 2)));

        assertThat(new Clustering.Similarity(0.5, 1, 2),
                greaterThan(new Clustering.Similarity(0.5, 0, 2)));

        assertThat(new Clustering.Similarity(0.5, 1, 2),
                lessThan(new Clustering.Similarity(0.5, 1, 3)));

        assertThat(new Clustering.Similarity(0.5, 1, 2),
                greaterThan(new Clustering.Similarity(0.5, 1, 1)));
    }
}
