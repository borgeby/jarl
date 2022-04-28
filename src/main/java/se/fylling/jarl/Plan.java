package se.fylling.jarl;

import java.util.List;
import java.util.Map;

public interface Plan {
    /**
     * @return The result as a map
     */
    List<Map<String, ?>> eval(Map<String, ?> input);
}
