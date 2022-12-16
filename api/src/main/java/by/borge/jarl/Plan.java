package by.borge.jarl;

import by.borge.jarl.internal.InternalPlan;

import java.util.Map;

/**
 * An executable plan, corresponding to an <code>entrypoint</code> of a compiled OPA IR.
 */
public class Plan {
    private final InternalPlan plan;

    Plan(InternalPlan plan) {
        this.plan = plan;
    }

    /**
     * Evaluate this plan given <code>input</code> and <code>data</code>.
     *
     * @param data the data document
     * @param input the input value
     * @return a {@link ResultSet} representing the evaluation result of this Plan
     */
    public ResultSet eval(Map<String, ?> data, Object input) {
        return ResultSet.fromResultsList(plan.eval(data, input));
    }
}
