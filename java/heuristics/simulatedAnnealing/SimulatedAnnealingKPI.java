package heuristics.simulatedAnnealing;

import java.util.List;

public record SimulatedAnnealingKPI (
        String instanceName,
        double initialTemperature,
        double coolingParameter,
        String coolingMethod,
        int epochLength,
        String terminationCriteria,
        double objective,
        double runTime,
        List<SAIteration> iterations
) {
}
