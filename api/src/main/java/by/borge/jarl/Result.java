package by.borge.jarl;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    /**
     * Gets the value of this {@link Result} as an instance of <code>type</code>.
     *
     * @param type the expected type of the returned value
     * @param <T> the type of the value
     * @return this Result's value
     * @throws ClassCastException if the value isn't <code>null</code> or of type <code>T</code>
     */
    public <T> T getValue(Class<T> type) {
        return type.cast(value);
    }

    /**
     * Gets the value of this {@link Result} as a {@link Map}.
     *
     * @return this Result's value as a Map
     * @throws ClassCastException if the value isn't <code>null</code> or a {@link Map}
     */
    public Map<String, ?> getValueAsMap() {
        Map<?, ?> map = getValue(Map.class);
        return map.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().toString(),
                Map.Entry::getValue
        ));
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
