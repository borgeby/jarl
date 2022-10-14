package by.borge.jarl;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JarlTests {
    @Test
    void simplePlan() throws IOException {
        var file = new File("../core/src/test/resources/rego/simple/plan.json");
        var jarl = new Jarl.Builder(file).build();
        var plan = jarl.getPlan("simple/q");
        var input = Map.of();
        Map<String, ?> data = Map.of();
        var resultSet = plan.eval(input, data);
        assertEquals(new ResultSet(new Result("bar")), resultSet);
    }
}
