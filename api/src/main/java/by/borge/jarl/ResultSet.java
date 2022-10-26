package by.borge.jarl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * The result-set of evaluating a {@link Plan}.
 */
public class ResultSet {
    private final List<Result> results;

    private static final ResultSet EMPTY = new ResultSet(List.of());

    ResultSet(List<Result> results) {
        this.results = requireNonNull(results, "results list must not be null");
    }

    ResultSet(Result result) {
        this(List.of(result));
    }

    /**
     * Returns an empty {@link ResultSet} with no {@link Result}s.
     *
     * @return an empty ResultSet
     */
    public static ResultSet empty() {
        return EMPTY;
    }

    /**
     * Constructs a {@link ResultSet} where each contained {@link Result} corresponds with
     * an item in the provided <code>results</code>.
     *
     * @param results the result values in the constructed ResultSet
     * @return a ResultSet containing a Result for each item in <code>results</code>
     */
    public static ResultSet of(Object... results) {
        return new ResultSet(Arrays.stream(results)
                .map(Result::new)
                .collect(Collectors.toList()));
    }

    public static ResultSet fromResultsMap(Map<String, ?> result) {
        return fromResultsList(List.of(result));
    }

    public static ResultSet fromResultsList(List<Map<String, ?>> list) {
        if (list == null) {
            return empty();
        }

        return new ResultSet(list.stream()
                .map(Result::fromResultMap)
                .collect(Collectors.toList()));
    }

    /**
     * Gets the list of {@link Result}s in this {@link ResultSet}.
     *
     * @return the list of {@link Result}s in this ResultSet
     */
    public List<Result> getResults() {
        return results;
    }

    /**
     * Gets the first {@link Result} in this {@link ResultSet}, or <code>null</code> if empty.
     * @return
     */
    public Result getFirst() {
        return results.stream().findFirst().orElse(null);
    }

    /**
     * Returns <code>true</code> if this result set has ha a single {@link Result} entry with
     * a boolean value of <code>true</code>; returns <code>false</code> otherwise.
     *
     * @return <code>true</code> if this set's only {@link Result} has the value <code>true</code>
     */
    public boolean allowed() {
        return results.size() == 1 && results.get(0).allowed();
    }

    @Override
    public String toString() {
        return "ResultSet{" +
                "results=" + results +
                '}';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ResultSet)) return false;
        ResultSet resultSet = (ResultSet) other;
        return results.equals(resultSet.results);
    }

    @Override
    public int hashCode() {
        return Objects.hash(results);
    }
}
