package aohanjanyan.search_clustering.utils;

import java.util.List;
import java.util.function.BiFunction;

/**
 * TODO
 */
public class Utils {
    /**
     * TODO
     *
     * @param a
     * @param b
     * @param acc
     * @param mapNext
     * @param mapEqual
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T extends Comparable<T>, R> R merge(List<T> a,
                                                       List<T> b,
                                                       R acc,
                                                       BiFunction<T, R, R> mapNext,
                                                       TerFunction<T, T, R, R> mapEqual) {
        int i = 0, j = 0;
        while (i < a.size() && j < b.size()) {
            int comparison = a.get(i).compareTo(b.get(j));
            if (comparison < 0) {
                acc = mapNext.apply(a.get(i++), acc);
            } else if (comparison > 0) {
                acc = mapNext.apply(b.get(j++), acc);
            } else {
                acc = mapEqual.apply(a.get(i++), b.get(j++), acc);
            }
        }
        if (i == a.size()) {
            a = b;
            i = j;
        }
        for (; i < a.size(); i++) {
            acc = mapNext.apply(a.get(i), acc);
        }
        return acc;
    }
}
