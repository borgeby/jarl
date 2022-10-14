package by.borge.jarl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ResultSet(List<Result> results) {
    private static final ResultSet EMPTY = new ResultSet(List.of());

    public ResultSet(Result result) {
        this(List.of(result));
    }

    public static ResultSet empty() {
        return EMPTY;
    }

    public static ResultSet from(Map<String, ?> result) {
        return from(List.of(result));
    }

    public static ResultSet from(List<Map<String, ?>> list) {
        if (list == null) {
            return empty();
        }

        return new ResultSet(list.stream()
                .map(Result::from)
                .collect(Collectors.toList()));
    }

    @Override
    public String toString() {
        return "ResultSet{" +
                "results=" + results +
                '}';
    }
}
