package aohanjanyan.search_clustering.utils;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class DisjointSetTest {
    @Test
    void testDefault() {
        DisjointSet disjointSet = new DisjointSet(10);
        for (int i = 0; i < 10; i++) {
            assertThat(disjointSet.find(i), equalTo(i));
        }
    }

    @Test
    void testUnion() {
        DisjointSet disjointSet = new DisjointSet(10);

        assertThat(disjointSet.union(0, 1), isOneOf(0, 1));
        assertThat(disjointSet.find(0), equalTo(disjointSet.find(1)));

        assertThat(disjointSet.union(2, 3), isOneOf(2, 3));
        assertThat(disjointSet.find(2), equalTo(disjointSet.find(3)));

        assertThat(disjointSet.find(0), not(equalTo(disjointSet.find(2))));
        assertThat(disjointSet.union(0, 2), isOneOf(0, 1, 2, 3));
        assertThat(disjointSet.find(0), allOf(
                equalTo(disjointSet.find(1)),
                equalTo(disjointSet.find(2)),
                equalTo(disjointSet.find(3))));
    }

    @Test
    void testUnionSame() {
        DisjointSet disjointSet = new DisjointSet(10);
        for (int i = 0; i < 10; i++) {
            assertThat(disjointSet.union(i, i), equalTo(i));
            assertThat(disjointSet.union(i, i), equalTo(i));
        }
    }

    @Test
    void testManyUnions() {
        final int SIZE = 1_000_000;
        final int CALL_N = 10_000_000;

        DisjointSet disjointSet = new DisjointSet(SIZE);
        Random random = new Random(0);

        for (int callI = 0; callI < CALL_N; callI++) {
            disjointSet.union(random.nextInt(SIZE), random.nextInt(SIZE));
        }
    }
}
