package aohanjanyan.search_clustering.algorithm;

import aohanjanyan.search_clustering.utils.DisjointSet;
import aohanjanyan.search_clustering.utils.Utils;

import java.util.*;

/**
 * Clustering algorithm described in
 * "Clustering Search Engine Query Log Containing Noisy Clickthroughs"
 * by Wing Shun Chan, Wai Ting Leung and Dik Lun Lee.
 */
public class Clustering {
    private double threshold;
    private MergeObserver mergeObserver;
    private DisjointSet leftSets, rightSets;

    /**
     * Constructs new clustering object.
     *
     * @param threshold threshold required for algorithm.
     */
    public Clustering(double threshold) {
        this.threshold = threshold;
    }

    /**
     * Set merge observer.
     *
     * @param mergeObserver merge observer.
     */
    public void setMergeObserver(MergeObserver mergeObserver) {
        this.mergeObserver = mergeObserver;
    }

    /**
     * Returns disjoint-set of clusters of the left part.
     * @return disjoint-set of clusters of the left part
     */
    public DisjointSet getLeftSets() {
        return leftSets;
    }

    /**
     * Returns disjoint-set of clusters of the right part.
     * @return disjoint-set of clusters of the right part
     */
    public DisjointSet getRightSets() {
        return rightSets;
    }

    /**
     * Performs clustering.
     * Use {@link #getLeftSets()} and {@link #getRightSets()} to get the results of clustering.
     * @param graph graph for clustering
     */
    public void cluster(BiGraph graph) {
        GraphPartData leftPartData = initGraphPartData(graph.left, graph.right);
        GraphPartData rightPartData = initGraphPartData(graph.left, graph.right);
        int mergeCount = 0;

        while (findMaxSimilarity(leftPartData) != null
                || findMaxSimilarity(rightPartData) != null) {

            Similarity maxSimilarity = findMaxSimilarity(leftPartData);
            if (maxSimilarity != null) {
                mergeNodes(graph.left, leftPartData, graph.right, rightPartData, maxSimilarity);
                if (mergeObserver != null) {
                    mergeObserver.nodesMerged(maxSimilarity.first, maxSimilarity.second,
                            true, ++mergeCount);
                }
            }

            maxSimilarity = findMaxSimilarity(rightPartData);
            if (maxSimilarity != null) {
                mergeNodes(graph.right, rightPartData, graph.left, leftPartData, maxSimilarity);
                if (mergeObserver != null) {
                    mergeObserver.nodesMerged(maxSimilarity.first, maxSimilarity.second,
                            false, ++mergeCount);
                }
            }
        }

        leftSets = leftPartData.clusterSets;
        rightSets = rightPartData.clusterSets;
    }

    // ---------------------------------------------------
    // These methods are package-private just for testing.
    //---------------------------------------------------

    Similarity findMaxSimilarity(GraphPartData partData) {
        if (partData.similarities.size() == 0) {
            return null;
        }
        return partData.similarities.descendingIterator().next();
    }

    void mergeNodes(BiGraph.Part part,
                    GraphPartData partData,
                    BiGraph.Part otherPart,
                    GraphPartData otherPartData,
                    Similarity similarity) {
        removeSimilaritiesWith(partData, similarity.first);
        removeSimilaritiesWith(partData, similarity.second);

        int newNodeI = partData.clusterSets.union(similarity.first, similarity.second);
        int removedNodeI = similarity.getAnother(newNodeI);

        List<BiGraph.Edge> newNodeEdges = new ArrayList<>();
        List<BiGraph.Edge> connectedToBoth = new ArrayList<>();
        List<Integer> connectedToNewNode = new ArrayList<>();
        List<Integer> connectedToRemovedNode = new ArrayList<>();

        Utils.merge(part.get(newNodeI).edges, part.get(removedNodeI).edges,
                Comparator.comparingInt(edge -> edge.dst),
                null,
                (edge, isNewNodeEdge, acc) -> {
                    newNodeEdges.add(edge);
                    if (isNewNodeEdge) {
                        connectedToNewNode.add(edge.dst);
                    } else {
                        connectedToRemovedNode.add(edge.dst);
                    }
                    return null;
                },
                (newNodeEdge, removedNodeEdge, acc) -> {
                    BiGraph.Edge newEdge = new BiGraph.Edge(
                            newNodeEdge.dst,
                            newNodeEdge.weight + removedNodeEdge.weight);
                    newNodeEdges.add(newEdge);
                    connectedToBoth.add(newEdge);
                    return null;
                });

        part.get(newNodeI).edges = newNodeEdges;
        part.get(removedNodeI).edges = null;

        // Update edges for intersection
        for (BiGraph.Edge edgeToOtherNode : connectedToBoth) {
            BiGraph.Node otherNode = otherPart.get(edgeToOtherNode.dst);
            otherNode.edges.removeIf(edge -> edge.dst == removedNodeI);
            otherNode.edges.stream()
                    .filter(edge -> edge.dst == newNodeI)
                    .findFirst()
                    .ifPresent(
                            edge -> edge.weight = edgeToOtherNode.weight
                    );
        }

        // Update edges to deleted node
        for (int otherNodeI : connectedToRemovedNode) {
            for (BiGraph.Edge edge : otherPart.get(otherNodeI).edges) {
                if (edge.dst == removedNodeI) {
                    edge.dst = newNodeI;
                }
            }
        }

        // Update similarities for intersection
        for (BiGraph.Edge edgeToOtherNode : connectedToBoth) {
            removeSimilaritiesWith(otherPartData, edgeToOtherNode.dst);
        }
        Set<Integer> processedIntersection = new HashSet<>();
        for (BiGraph.Edge edgeToOtherNode : connectedToBoth) {
            processedIntersection.add(edgeToOtherNode.dst);

            for (int siblingI : siblings(otherPart, part, edgeToOtherNode.dst)) {
                if (!processedIntersection.contains(siblingI)) {
                    insertSimilarity(otherPart, otherPartData, edgeToOtherNode.dst, siblingI);
                }
            }
        }

        // Calculate similarities for symmetrical difference
        for (int otherNode1I : connectedToNewNode) {
            for (int otherNode2I : connectedToRemovedNode) {
                insertSimilarity(otherPart, otherPartData, otherNode1I, otherNode2I);
            }
        }

        // Calculate similarities for new node
        for (int siblingI : siblings(part, otherPart, newNodeI)) {
            insertSimilarity(part, partData, newNodeI, siblingI);
        }
    }

    static void removeSimilaritiesWith(GraphPartData partData, int nodeI) {
        for (Similarity similarity : partData.nodesSimilarities.get(nodeI)) {
            partData.similarities.remove(similarity);
            partData.nodesSimilarities
                    .get(similarity.getAnother(nodeI))
                    .remove(similarity);
        }
        partData.nodesSimilarities.get(nodeI).clear();
    }

    GraphPartData initGraphPartData(BiGraph.Part part,
                                    BiGraph.Part otherPart) {
        GraphPartData partData = new GraphPartData();

        partData.clusterSets = new DisjointSet(part.size());

        partData.similarities = new TreeSet<>();
        partData.nodesSimilarities = new ArrayList<>(part.size());

        for (int nodeI = 0; nodeI < part.size(); nodeI++) {
            partData.nodesSimilarities.add(new HashSet<>());
        }

        for (int nodeI = 0; nodeI < part.size(); nodeI++) {
            for (int siblingI : siblings(part, otherPart, nodeI)) {
                if (siblingI > nodeI) {
                    insertSimilarity(part, partData, nodeI, siblingI);
                }
            }
        }

        return partData;
    }

    static Iterable<Integer> siblings(BiGraph.Part part,
                                      BiGraph.Part otherPart,
                                      int nodeI) {
        return () -> part.get(nodeI).edges.stream()
                .flatMap(edge -> otherPart.get(edge.dst).edges.stream())
                .map(edge -> edge.dst)
                .filter(siblingI -> siblingI != nodeI)
                .distinct()
                .iterator();
    }

    void insertSimilarity(BiGraph.Part part,
                          GraphPartData partData,
                          int nodeI,
                          int siblingI) {
        Similarity similarity = new Similarity(
                calculateSimilarity(part.get(nodeI), part.get(siblingI)),
                nodeI,
                siblingI);
        if (similarity.similarity < threshold) {
            return;
        }
        partData.nodesSimilarities.get(nodeI).add(similarity);
        partData.nodesSimilarities.get(siblingI).add(similarity);
        partData.similarities.add(similarity);
    }

    static double calculateSimilarity(BiGraph.Node node,
                                      BiGraph.Node sibling) {
        IntPair ratio = Utils.merge(node.edges, sibling.edges,
                Comparator.comparingInt(edge -> edge.dst),
                new IntPair(),
                (edge, isNodeEdge, tmpRatio) -> {
                    tmpRatio.second += edge.weight;
                    return tmpRatio;
                },
                (nodeEdge, siblingEdge, tmpRatio) -> {
                    tmpRatio.first += nodeEdge.weight + siblingEdge.weight;
                    tmpRatio.second += nodeEdge.weight + siblingEdge.weight;
                    return tmpRatio;
                });
        return ((double) ratio.first) / ((double) ratio.second);
    }

    /**
     * Class for observing the progress of the algorithm.
     */
    public interface MergeObserver {
        /**
         * Called after each merge operation.
         * @param i index of the first node which was merged
         * @param j index of the second node which was merged
         * @param isLeftPart {@code true} if called after merging nodes in the left part,
         *                   {@code false} otherwise
         * @param mergeI number of performed merges
         */
        void nodesMerged(int i, int j, boolean isLeftPart, int mergeI);
    }

    static class IntPair {
        int first, second;
    }

    static class GraphPartData {
        DisjointSet clusterSets;
        NavigableSet<Similarity> similarities;
        List<Set<Similarity>> nodesSimilarities;
    }

    static class Similarity implements Comparable<Similarity> {
        double similarity;
        int first, second;

        Similarity(double similarity, int first, int second) {
            this.similarity = similarity;
            this.first = first;
            this.second = second;
        }

        int getAnother(int one) {
            return first - one + second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Similarity that = (Similarity) o;

            return Double.compare(that.similarity, similarity) == 0
                    && first == that.first && second == that.second;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            temp = Double.doubleToLongBits(similarity);
            result = (int) (temp ^ (temp >>> 32));
            result = 31 * result + first;
            result = 31 * result + second;
            return result;
        }

        @Override
        public int compareTo(Similarity that) {
            if (this.similarity < that.similarity) {
                return -1;
            } else if (this.similarity > that.similarity) {
                return 1;
            }

            if (this.first < that.first) {
                return -1;
            } else if (this.first > that.first) {
                return 1;
            }

            if (this.second < that.second) {
                return -1;
            } else if (this.second > that.second) {
                return 1;
            }
            return 0;
        }
    }
}
