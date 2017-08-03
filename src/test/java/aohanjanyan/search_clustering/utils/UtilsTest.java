package aohanjanyan.search_clustering.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class UtilsTest {
    @Test
    void testMergeSorting() {
        new Utils();

        assertThat(Utils.merge(
                Arrays.asList(1, 3, 5, 7, 9),
                Arrays.asList(2, 4, 6, 8, 9),
                Comparator.naturalOrder(),
                new ArrayList<>(),
                (next, isA, acc) -> {
                    acc.add(next);
                    assertThat("wrong merge order", isA, equalTo(next % 2 != 0));
                    return acc;
                },
                (nextA, nextB, acc) -> {
                    assertThat(nextA, equalTo(nextB));
                    acc.add(nextA);
                    return acc;
                }), equalTo(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9)));

        TerFunction<Integer, Boolean, ArrayList<Integer>, ArrayList<Integer>> mapNext =
                (next, isA, acc) -> {
                    acc.add(next);
                    return acc;
                };
        TerFunction<Integer, Integer, ArrayList<Integer>, ArrayList<Integer>> mapEqual =
                (nextA, nextB, acc) -> {
                    acc.add(nextA);
                    return acc;
                };

        assertThat(Utils.merge(
                Arrays.asList(1, 2, 3),
                Arrays.asList(4, 5, 6),
                Comparator.naturalOrder(),
                new ArrayList<>(),
                mapNext,
                mapEqual), equalTo(Arrays.asList(1, 2, 3, 4, 5, 6)));

        assertThat(Utils.merge(
                Arrays.asList(4, 5, 6),
                Arrays.asList(1, 2, 3),
                Comparator.naturalOrder(),
                new ArrayList<>(),
                mapNext,
                mapEqual), equalTo(Arrays.asList(1, 2, 3, 4, 5, 6)));
    }
}
