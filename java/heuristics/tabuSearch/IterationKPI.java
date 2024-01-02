package heuristics.tabuSearch;

public record IterationKPI(
        int iteration,
        double bestObjective,
        double currentObjective
) {
}
