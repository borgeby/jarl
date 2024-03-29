package by.borge.jarl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JarlTests {
    private static Stream<Arguments> testInputAndData() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("foo", null),
                Arguments.of(null, "bar"),
                Arguments.of("foo", "bar"),
                Arguments.of(1337, 42),
                Arguments.of(13.37, 4.2),
                Arguments.of(true, false),
                Arguments.of(false, true),
                Arguments.of(List.of(), List.of()),
                Arguments.of(
                        List.of(1, "two", 3.0),
                        List.of(4, "five", 6.0)),
                Arguments.of(
                        Map.of(
                                "foo", "bar",
                                "one", 1,
                                "list", List.of(1, 2, 3),
                                "map", Map.of("a", "b")),
                        Map.of(
                                "bar", "foo",
                                "two", 2,
                                "list", List.of(4, 5, 6),
                                "map", Map.of("c", "d")))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testInputAndData(Object input, Object dataAttributes) throws IOException {
        var file = new File(getClass().getResource("/rego/echo/plan.json").getFile());
        var jarl = Jarl.builder(file).build();
        var plan = jarl.getPlan("echo");
        Map<String, ?> data = dataAttributes != null ? Map.of("attributes", dataAttributes) : null;
        var resultSet = plan.eval(input, data);

        assertEquals(1, resultSet.getResults().size());
        var value = resultSet.getFirst().getValueAsMap();
        assertEquals(input, value.get("i"));
        assertEquals(dataAttributes, value.get("d"));
    }

    private static Stream<Arguments> testResultAsList() {
        return Stream.of(
                Arguments.of(List.of(0, 1, 2, 3), Integer.class),
                Arguments.of(List.of(0.0, 1.1, 2.2, 3.3), Double.class),
                Arguments.of(List.of(0.0, 1.1, 2.2, 3.3), Number.class),
                Arguments.of(List.of("foo", "bar", "baz"), String.class),
                Arguments.of(List.of(true, false), Boolean.class)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testResultAsList(Object input, Class<?> type) throws IOException {
        var file = new File(getClass().getResource("/rego/echo/plan.json").getFile());
        var jarl = Jarl.builder(file).build();
        var plan = jarl.getPlan("echo/i");
        var resultSet = plan.eval(input, null);

        var value = resultSet.getFirst().getValueAsList(type);
        assertEquals(input, value);
    }

    @Test
    void testResultAsBoolean() throws IOException {
        var file = new File(getClass().getResource("/rego/echo/plan.json").getFile());
        var jarl = Jarl.builder(file).build();
        var plan = jarl.getPlan("echo/i");

        var resultSet = plan.eval(true, null);
        var value = resultSet.getFirst().getValueAsBoolean();
        assertTrue(value);

        resultSet = plan.eval(false, null);
        value = resultSet.getFirst().getValueAsBoolean();
        assertFalse(value);
    }
}
