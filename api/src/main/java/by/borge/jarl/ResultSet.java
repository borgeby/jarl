package by.borge.jarl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class ResultSet {
    private final List<Result> results;

    private static final ResultSet EMPTY = new ResultSet(List.of());

    public ResultSet(List<Result> results) {
        this.results = requireNonNull(results, "results list must not be null");
    }

    public ResultSet(Result result) {
        this(List.of(result));
    }

    public static ResultSet empty() {
        return EMPTY;
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

    public List<Result> getResults() {
        return results;
    }

    public Result getFirst() {
        return results.stream().findFirst().orElse(null);
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
