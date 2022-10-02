package by.borge.jarl;

import java.util.Map;

public record Result(Object value) {
    public static Result from(Map<String, ?> map) {
        return new Result(map.get("result"));
    }

    @Override
    public String toString() {
        return "Result{" +
                "value=" + value +
                '}';
    }
}
