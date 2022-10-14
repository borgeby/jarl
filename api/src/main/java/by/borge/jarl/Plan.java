package by.borge.jarl;

import by.borge.jarl.internal.InternalPlan;

import java.util.Map;

public class Plan {
    private final InternalPlan plan;

    Plan(InternalPlan plan) {
        this.plan = plan;
    }

    public ResultSet eval(Object input, Map<String, ?> data) {
        return ResultSet.from(plan.eval(input, data));
    }
}
