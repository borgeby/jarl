package by.borge.jarl;

import clojure.lang.PersistentHashMap;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JarlTests {
    @Test
    void simplePlanQ() throws IOException {
        var file = new File("../core/src/test/resources/rego/simple/plan.json");
        var jarl = Jarl.builder(file).build();
        var plan = jarl.getPlan("simple/q");
        var input = Map.of("user", "alice");
        Map<String, ?> data = Map.of();
        var resultSet = plan.eval(input, data);
        assertEquals(new ResultSet(new Result("bar")), resultSet);
        assertEquals("bar", resultSet.getFirst().getValue());
        assertFalse(resultSet.allowed());
    }

    @Test
    void simplePlanP() throws IOException {
        var file = new File("../core/src/test/resources/rego/simple/plan.json");
        var jarl = Jarl.builder(file).build();
        var plan = jarl.getPlan("simple/p");
        var input = (Map) PersistentHashMap.create(Map.of("x", 1337));
        Map<String, ?> data = Map.of();
        var resultSet = plan.eval(data, input);
        assertEquals(new ResultSet(new Result(true)), resultSet);
        assertEquals(true, resultSet.getFirst().getValue());
        assertTrue(resultSet.allowed());
    }
}
