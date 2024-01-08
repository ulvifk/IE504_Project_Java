public record SASetting (
        int seed,
        double initialTemperature,
        double coolingParameter,
        String coolingMethod,
        int epochLength,
        String terminationCriteria
) {
}
