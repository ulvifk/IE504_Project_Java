package heuristics.tabuSearch;

import java.util.List;

public record TabuKPI(String instance,
                      int tabuTenure,
                      int maxIteration,
                      double beforeIntensificationObjective,
                      double afterIntensificationObjective,
                      List<IterationKPI> iterationKPIs) {

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
