package heuristics.tabuSearch;

public record TabuIntensificationSetting (
        int level,
        int searchNBest,
        int searchNNeighborhood
){
}
