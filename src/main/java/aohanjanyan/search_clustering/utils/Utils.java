package aohanjanyan.search_clustering.utils;

import java.util.Comparator;
import java.util.List;

/**
 * TODO
 */
public class Utils {
    /**
     * TODO
     *
     * @param a
     * @param b
     * @param comparator
     * @param acc
     * @param mapNext
     * @param mapEqual
     * @param <T>
     * @param <R>
     * @return
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
