package aohanjanyan.search_clustering.utils;

import java.util.Comparator;
import java.util.List;

/**
 * Class with utility functions.
 */
public class Utils {
    /**
     * Generalized merge algorithm.
     *
     * @param a          first list
     * @param b          second list
     * @param comparator comparator on elements of lists.
     *                   {@code a} and {@code b} should be sorted according to this comparator.
     * @param acc        default value of accumulator
     * @param mapNext    action to perform upon meeting next unique element.
     *                   First passed argument is the next element itself.
     *                   Second argument is {@code true} if the next element is from {@code a},
     *                   {@code false} otherwise,
     *                   Third argument is accumulator.
     *                   New value of accumulator should be returned.
     * @param mapEqual   action to perform upon meeting next element, which is present in both
     *                   {@code a} and {@code b}.
     *                   First and second arguments are the next elements.
     *                   Third argument is accumulator.
     *                   New value of accumulator should be returned.
     * @param <T>        type of lists' elements
     * @param <R>        return type
     * @return the result of the algorithm (the last value of accumulator).
     */
    public static <T, R> R merge(List<T> a,
                                 List<T> b,
                                 Comparator<T> comparator,
                                 R acc,
                                 TerFunction<T, Boolean, R, R> mapNext,
                                 TerFunction<T, T, R, R> mapEqual) {
        int i = 0, j = 0;
        while (i < a.size() && j < b.size()) {
            int comparison = comparator.compare(a.get(i), b.get(j));
            if (comparison < 0) {
                acc = mapNext.apply(a.get(i++), true, acc);
            } else if (comparison > 0) {
                acc = mapNext.apply(b.get(j++), false, acc);
            } else {
                acc = mapEqual.apply(a.get(i++), b.get(j++), acc);
            }
        }
        boolean isA = true;
        if (i == a.size()) {
            a = b;
            i = j;
            isA = false;
        }
        for (; i < a.size(); i++) {
            acc = mapNext.apply(a.get(i), isA, acc);
        }
        return acc;
    }
}
