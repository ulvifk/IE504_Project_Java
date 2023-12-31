package heuristics.tabuSearch;

import java.util.List;

public record TabuKPI(String instance,
                      int seed,
                      int tabuTenure,
                      int maxIteration,
                      TabuIntensificationSetting intensificationSetting,
                      DiversificationSetting diversificationSetting,
                      double beforeIntensificationObjective,
                      double afterIntensificationObjective,
                      List<TabuIterationKPI> iterationKPIs,
                      List<TabuIterationKPI> iterationKPIsBeforeDiversification,
                      List<TabuIterationKPI> iterationKPIsAfterDiversification,
                      double cpuTime,
                      double intensificationCpuTime) {

    public String getRowString(){
        StringBuilder sb = new StringBuilder();
        sb.append(instance).append(",");
        sb.append(tabuTenure).append(",");
        sb.append(maxIteration).append(",");
        sb.append(beforeIntensificationObjective).append(",");
        sb.append(afterIntensificationObjective).append(",");
        sb.append(iterationKPIs.size()).append(",");
        return sb.toString();
    }
}
