package aohanjanyan.search_clustering.utils;

/**
 * Represents a function that accepts three arguments and produces a result.
 *
 * @param <A> the type of the first argument to the function
 * @param <B> the type of the second argument to the function
 * @param <C> the type of the third argument to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface TerFunction<A, B, C, R> {
    /**
     * Applies this function to the given arguments.
     *
     * @param a the first function argument
     * @param b the second function argument
     * @param c the third function argument
     * @return the function result
     */
    R apply(A a, B b, C c);
}
