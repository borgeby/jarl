package by.borge.jarl;

import java.util.Map;
import java.util.Objects;

public class Result {
    private final Object value;

    public Result(Object value) {
        this.value = value;
    }

    public static Result fromResultMap(Map<String, ?> map) {
        return new Result(map.get("result"));
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Result{" +
                "value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Result)) return false;
        Result result = (Result) other;
        return Objects.equals(value, result.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
