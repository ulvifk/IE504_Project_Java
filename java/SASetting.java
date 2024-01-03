public record SASetting (
        double initialTemperature,
        double coolingParameter,
        String coolingMethod,
        int epochLength,
        String terminationCriteria
) {
}
