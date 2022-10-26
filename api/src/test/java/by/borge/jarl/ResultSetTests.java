package by.borge.jarl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

public class ResultSetTests {
    private static Stream<Arguments> allowed() {
        return Stream.of(
                of(ResultSet.empty(), false),
                of(ResultSet.of(), false),
                of(ResultSet.of((Object) null), false),
                of(ResultSet.of(false), false),
                of(ResultSet.of(true), true),
                of(ResultSet.of("true"), false),
                of(ResultSet.of("false"), false),
                of(ResultSet.of(42), false),
                of(ResultSet.of(13.37), false),
                of(ResultSet.of(true, false), false),
                of(ResultSet.of(false, true), false),
                of(ResultSet.of(true, true), false),
                of(ResultSet.of(true, "true"), false),
                of(ResultSet.of(true, "false"), false),
                of(ResultSet.of(true, 13.37), false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void allowed(ResultSet input, boolean expected) {
        assertEquals(expected, input.allowed());
    }
}
