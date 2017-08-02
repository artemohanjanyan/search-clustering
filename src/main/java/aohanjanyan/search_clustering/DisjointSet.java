package aohanjanyan.search_clustering;

import java.util.Arrays;

/**
 * TODO
 */
public class DisjointSet {
    private int[] parents;
    private int[] sizes;

    public DisjointSet(int n) {
        parents = new int[n];
        Arrays.fill(parents, -1);
        sizes = new int[n];
        Arrays.fill(sizes, 1);
    }

    public int find(int i) {
        if (parents[i] == -1) {
            return i;
        } else {
            return parents[i] = find(parents[i]);
        }
    }

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
