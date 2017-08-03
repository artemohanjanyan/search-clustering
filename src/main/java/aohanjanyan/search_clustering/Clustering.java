package aohanjanyan.search_clustering;

import aohanjanyan.search_clustering.utils.DisjointSet;
import aohanjanyan.search_clustering.utils.Utils;

import java.util.*;

public class Clustering {
    private final double threshold;
    private MergeObserver mergeObserver;
    private DisjointSet leftSets, rightSets;

    public Clustering(double threshold) {
        this.threshold = threshold;
    }

    public void setMergeObserver(MergeObserver mergeObserver) {
        this.mergeObserver = mergeObserver;
    }

    public DisjointSet getLeftSets() {
        return leftSets;
    }

    public DisjointSet getRightSets() {
        return rightSets;
    }

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
                            true, mergeCount++);
                }
            }

            maxSimilarity = findMaxSimilarity(rightPartData);
            if (maxSimilarity != null) {
                mergeNodes(graph.right, rightPartData, graph.left, leftPartData, maxSimilarity);
                if (mergeObserver != null) {
                    mergeObserver.nodesMerged(maxSimilarity.first, maxSimilarity.second,
                            false, mergeCount++);
                }
            }
        }

        leftSets = leftPartData.clusterSets;
        rightSets = rightPartData.clusterSets;
    }

    private Similarity findMaxSimilarity(GraphPartData partData) {
        if (partData.similarities.size() == 0) {
            return null;
        }
        Similarity maxSimilarity = partData.similarities.descendingIterator().next();
        if (maxSimilarity.similarity < threshold) {
            maxSimilarity = null;
        }
        return maxSimilarity;
    }

    private static void mergeNodes(BiGraph.Part part,
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
            for (BiGraph.Edge otherNodeEdge : otherNode.edges) {
                if (otherNodeEdge.dst == newNodeI) {
                    otherNodeEdge.weight = edgeToOtherNode.weight;
                    break;
                }
            }
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

            for (BiGraph.Edge edge : otherPart.get(edgeToOtherNode.dst).edges) {
                for (BiGraph.Edge edgeToSibling : part.get(edge.dst).edges) {
                    if (!processedIntersection.contains(edgeToSibling.dst)) {
                        insertSimilarity(otherPart, otherPartData,
                                edgeToOtherNode.dst,
                                edgeToSibling.dst);
                    }
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
        for (BiGraph.Edge edge : part.get(newNodeI).edges) {
            for (BiGraph.Edge edgeToSibling : otherPart.get(edge.dst).edges) {
                insertSimilarity(part, partData, newNodeI, edgeToSibling.dst);
            }
        }
    }

    private static void removeSimilaritiesWith(GraphPartData partData, int nodeI) {
        for (Similarity similarity : partData.nodesSimilarities.get(nodeI)) {
            partData.similarities.remove(similarity);
            partData.nodesSimilarities
                    .get(similarity.getAnother(nodeI))
                    .remove(similarity);
        }
        partData.nodesSimilarities.get(nodeI).clear();
    }

    private static GraphPartData initGraphPartData(BiGraph.Part part,
                                                   BiGraph.Part otherPart) {
        GraphPartData partData = new GraphPartData();

        partData.clusterSets = new DisjointSet(part.size());

        partData.similarities = new TreeSet<>();
        partData.nodesSimilarities = new ArrayList<>(part.size());

        for (int nodeI = 0; nodeI < part.size(); nodeI++) {
            partData.nodesSimilarities.add(new HashSet<>());
        }

        for (int nodeI = 0; nodeI < part.size(); nodeI++) {
            Set<Integer> processedSiblings = new HashSet<>();
//            for (int siblingI : siblings(part, otherPart, nodeI)) {
//                if (!processedSiblings.contains(siblingI) && siblingI > nodeI) {
//                    insertSimilarity(part, partData, nodeI, siblingI);
//                    processedSiblings.add(siblingI);
//                }
//            }
            for (BiGraph.Edge edge : part.get(nodeI).edges) {
                for (BiGraph.Edge edgeToSibling : otherPart.get(edge.dst).edges) {
                    if (!processedSiblings.contains(edgeToSibling.dst)
                            && edgeToSibling.dst > nodeI) {
                        insertSimilarity(part, partData, nodeI, edgeToSibling.dst);
                        processedSiblings.add(edgeToSibling.dst);
                    }
                }
            }
        }

        return partData;
    }

//    private static Iterable<Integer> siblings(BiGraph.Part part,
//                                              BiGraph.Part otherPart,
//                                              int nodeI) {
//        return () -> part.get(nodeI).edges.stream()
//                .flatMap(edge -> otherPart.get(edge.dst).edges.stream())
//                .map(edge -> edge.dst)
//                .distinct()
//                .iterator();
//    }

    private static void insertSimilarity(BiGraph.Part part,
                                         GraphPartData graphPartData,
                                         int nodeI,
                                         int siblingI) {
        Similarity similarity = new Similarity(
                calculateSimilarity(part.get(nodeI), part.get(siblingI)),
                nodeI,
                siblingI);
        graphPartData.nodesSimilarities.get(nodeI).add(similarity);
        graphPartData.nodesSimilarities.get(siblingI).add(similarity);
        graphPartData.similarities.add(similarity);
    }

    private static double calculateSimilarity(BiGraph.Node node,
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
        if (ratio.second == 0) {
            return 0;
        } else {
            return ((double) ratio.first) / ((double) ratio.second);
        }
    }

    public interface MergeObserver {
        void nodesMerged(int i, int j, boolean isLeftPart, int mergeI);
    }

    private static class IntPair {
        int first, second;
    }

    private static class GraphPartData {
        DisjointSet clusterSets;
        NavigableSet<Similarity> similarities;
        List<Set<Similarity>> nodesSimilarities;
    }

    private static class Similarity implements Comparable<Similarity> {
        double similarity;
        int first, second;

        public Similarity(double similarity, int first, int second) {
            this.similarity = similarity;
            this.first = first;
            this.second = second;
        }

        public int getAnother(int one) {
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
