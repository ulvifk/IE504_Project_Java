package heuristics.tabuSearch;

public record TabuIterationKPI(
        int iteration,
        double bestObjective,
        double currentObjective,
        double cpuTime
) {
}
