package by.borge.jarl;

import java.util.Map;
import java.util.Objects;

/**
 * The result of evaluating a {@link Plan}.
 */
public class Result {
    private final Object value;

    public Result(Object value) {
        this.value = value;
    }

    public static Result fromResultMap(Map<String, ?> map) {
        return new Result(map.get("result"));
    }

    /**
     * Gets the value of this {@link Result}.
     *
     * @return this Result's value
     */
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

    /**
     * Returns <code>true<</code> if this result has the boolean value <code>true</code>;
     * returns <code>false</code> otherwise.
     *
     * @return <code>true</code> if this result has the boolean value <code>true</code>
     */
    public boolean allowed() {
        return value instanceof Boolean && (Boolean) value;
    }
}
