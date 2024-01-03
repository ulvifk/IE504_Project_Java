package heuristics.simulatedAnnealing;

public record SAIteration (
        int iteration,
        double temperature,
        double currentObjective,
        double bestObjective,
        double runtime
) {
}
