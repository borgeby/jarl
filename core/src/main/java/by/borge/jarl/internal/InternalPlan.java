package by.borge.jarl.internal;

import java.util.List;
import java.util.Map;

public interface InternalPlan {
    /**
     * @return The result as a map
     */
    List<Map<String, ?>> eval(Map<String, ?> data, Object input);
}
