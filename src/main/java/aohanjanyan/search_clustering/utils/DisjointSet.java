package aohanjanyan.search_clustering.utils;

import java.util.Arrays;

/**
 * Represents disjoint-set data structure.
 */
public class DisjointSet {
    private int[] parents;
    private int[] sizes;

    /**
     * Constructs disjoint-set of fixed size.
     * Each element is placed in individual set with the same index.
     *
     * @param n size
     */
    public DisjointSet(int n) {
        parents = new int[n];
        Arrays.fill(parents, -1);
        sizes = new int[n];
        Arrays.fill(sizes, 1);
    }

    /**
     * Returns set, which contains provided element.
     * @param i index of element
     * @return index of set containing {@code i}
     */
    public int find(int i) {
        if (parents[i] == -1) {
            return i;
        } else {
            return parents[i] = find(parents[i]);
        }
    }

    /**
     * Unite sets, which contain provided elements.
     * Index of the resulting set is guaranteed to be equal to
     * one of the indexes of arguments' sets.
     * @param i index of the first element
     * @param j index of the second element
     * @return index of new set
     */
    public int union(int i, int j) {
        int iRoot = find(i), jRoot = find(j);
        if (iRoot == jRoot) {
            return iRoot;
        }
        if (sizes[iRoot] < sizes[jRoot]) {
            int tmp = iRoot;
            iRoot = jRoot;
            jRoot = tmp;
        }
        parents[jRoot] = iRoot;
        sizes[iRoot] += sizes[jRoot];
        return iRoot;
    }
}
