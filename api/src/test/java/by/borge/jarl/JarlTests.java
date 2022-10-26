package by.borge.jarl;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class JarlTests {
    @Test
    void simplePlan() throws IOException {
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
}
