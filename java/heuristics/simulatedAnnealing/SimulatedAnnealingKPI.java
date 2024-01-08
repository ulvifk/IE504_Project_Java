package heuristics.simulatedAnnealing;

import java.util.List;

public record SimulatedAnnealingKPI (
        String instanceName,
        int seed,
        double initialTemperature,
        double coolingParameter,
        String coolingMethod,
        int epochLength,
        String terminationCriteria,
        double objective,
        double bestObjective,
        double runTime,
        List<SAIteration> iterations
) {
}
