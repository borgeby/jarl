package by.borge.jarl.internal;

import java.util.Map;

public interface IntermediateRepresentation {
    IntermediateRepresentation withStrictBuiltinErrors(boolean strict);
    Map<String, InternalPlan> getPlans();
    InternalPlan getPlan(String entryPoint);
}
