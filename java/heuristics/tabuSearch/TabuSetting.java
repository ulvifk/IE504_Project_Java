package heuristics.tabuSearch;

public record TabuSetting (
        int tabuTenure,
        int seed,
        int maxIteration,
        TabuIntensificationSetting intensificationSetting,
        DiversificationSetting diversificationSetting
){
}
