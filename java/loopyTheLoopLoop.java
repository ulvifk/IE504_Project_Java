import com.google.gson.GsonBuilder;
import data.ProblemData;
import heuristics.GreedyHeuristic;
import heuristics.simulatedAnnealing.SimulatedAnnealing;
import heuristics.simulatedAnnealing.SimulatedAnnealingKPI;
import heuristics.tabuSearch.TabuKPI;
import heuristics.tabuSearch.TabuSearch;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class loopyTheLoopLoop {
    public static void main(String[] args) throws Exception {
        var problems = Main.readJsonFiles("./java/dataset");
        //tabuLoop(problems);
        saLoop(problems);
    }

    private static void tabuLoop(List<ProblemData> problems) throws FileNotFoundException {
        var tabuTenures = new int[]{20};

        var tabuKPIs = new LinkedList<>();
        var problemIndices = new int[]{0, 1};

        for (var problemIndex : problemIndices) {
            for (var tabuTenure : tabuTenures) {
                var tabuSetting = new TabuSetting(tabuTenure, 200);
                var problem = problems.get(problemIndex);

                var tabuKPI = runTabuSearch(problem, tabuSetting);
                tabuKPIs.add(tabuKPI);
            }
        }

        toJson(tabuKPIs, "tabuKPIs.json");
    }

    private static void saLoop(List<ProblemData> problems) throws Exception {
        var temperatures = new int[]{5000};
        var coolingParameters = new double[]{0.01};

        var saKPIs = new LinkedList<>();
        var problemIndices = new int[]{0, 1};

        for (var problemIndex : problemIndices) {
            for (var temperatur : temperatures) {
                for (var coolingParameter : coolingParameters) {
                    var saSetting = new SASetting(temperatur, coolingParameter, "geometric", 2, "minT,1");
                    var problem = problems.get(problemIndex);

                    var saKPI = runSA(problem, saSetting);
                    saKPIs.add(saKPI);
                }
            }
        }

        toJson(saKPIs, "saKPIs.json");
    }

    private static SimulatedAnnealingKPI runSA(ProblemData problemData, SASetting saSetting) throws Exception {
        var greedyHeury = new GreedyHeuristic(problemData);
        var initialSolution = greedyHeury.solution;

        var simulatedAnnealing = new SimulatedAnnealing(problemData, saSetting.initialTemperature(),
                saSetting.coolingParameter(), saSetting.coolingMethod(), saSetting.epochLength(),
                saSetting.terminationCriteria(), initialSolution);

        simulatedAnnealing.run(0, 0);

        return simulatedAnnealing.kpi;
    }

    private static TabuKPI runTabuSearch(ProblemData problemData, TabuSetting tabuSetting){
        System.out.println("-----------------------------------------------------------------");

        var greedyHeury = new GreedyHeuristic(problemData);
        var initialSolution = greedyHeury.solution;

        var tabuSearch = new TabuSearch(problemData, initialSolution, tabuSetting.tabuTenure());
        tabuSearch.solve(tabuSetting.maxIteration());

        return tabuSearch.tabuKPI;
    }

    private static void writeTabuKPIsToCsv(List<TabuKPI> tabuKPIs, String fileName) throws FileNotFoundException {
        var out = new PrintWriter(fileName);
        out.println("instance,tabuTenure,maxIteration,beforeIntensificationObjective,afterIntensificationObjective,iterationKPIsSize\n");
        for (var tabuKPI : tabuKPIs) {
            out.println(tabuKPI.getRowString());
        }

        out.close();
    }

    private static void toJson(List<Object> kpis, String fileName) throws FileNotFoundException {
        var gson = new GsonBuilder().setPrettyPrinting().create();

        var json = gson.toJson(kpis);

        var out = new PrintWriter(fileName);
        out.println(json);
        out.close();
    }
}
