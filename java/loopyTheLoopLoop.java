import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import data.ProblemData;
import heuristics.GreedyHeuristic;
import heuristics.simulatedAnnealing.SimulatedAnnealing;
import heuristics.simulatedAnnealing.SimulatedAnnealingKPI;
import heuristics.tabuSearch.*;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class loopyTheLoopLoop {
    public static void main(String[] args) throws Exception {
        var problems = Main.readJsonFiles("./clustered_customers");
//        tabuLoop(problems, "clustered_customers");
        saLoop(problems, "clustered_customers");

        problems = Main.readJsonFiles("./random_customers");
//        tabuLoop(problems, "random_customers");
        saLoop(problems, "random_customers");
    }

    private static void tabuLoop(List<ProblemData> problems, String key) throws IOException {
        var tabuKPIs = new LinkedList<>();
        var problemIndices = new int[]{1, 2, 3};
        var seeds = new int[]{0, 1, 2};

        var tabuTenures = new int[]{10, 20, 50};
        var intensificationLevels = new int[]{2};
        var nNumberOfNonImprovingIterations = new int[]{50};
        var tabuIntensificationSearchNBests = new int[]{5, 10};
        var tabuIntensificationSearchNNeighborhoods = new int[]{5};

        for (var problem : problems) {
            var problemIndex = problems.indexOf(problem);
            for (var seed : seeds) {
                for (var tabuTenure : tabuTenures) {
                    for (var nNumberOfNonImprovingIteration : nNumberOfNonImprovingIterations) {
                        for (var tabuIntensificationSearchNBest : tabuIntensificationSearchNBests) {
                            for (var tabuIntensificationSearchNNeighborhood : tabuIntensificationSearchNNeighborhoods) {
                                for (var intensificationLevel : intensificationLevels) {
                                    var intensificationSetting = new TabuIntensificationSetting(
                                            intensificationLevel,
                                            tabuIntensificationSearchNBest,
                                            tabuIntensificationSearchNNeighborhood);
                                    var diversificationSetting = new DiversificationSetting(
                                            nNumberOfNonImprovingIteration
                                    );

                                    var tabuSetting = new TabuSetting(tabuTenure,
                                            seed,
                                            250,
                                            intensificationSetting,
                                            diversificationSetting);

                                    //System.out.println("Problem Size: " + problem.customers.size());
                                    var tabuKPI = runTabuSearch(problem, tabuSetting);
                                    tabuKPIs.add(tabuKPI);
                                    System.out.println(problemIndex + ", " + seed + ", " + tabuTenure + ", " + nNumberOfNonImprovingIteration + ", " + tabuIntensificationSearchNBest + ", " + tabuIntensificationSearchNNeighborhood + ", " + intensificationLevel + ", " + tabuKPI.cpuTime() + ", " + tabuKPI.intensificationCpuTime());
                                }
                            }
                        }
                    }
                }
            }
        }

        toJson(tabuKPIs, key + "_tabuKPIs.json");
    }

    private static void saLoop(List<ProblemData> problems, String key) throws Exception {
        var temperatures = new int[]{50000};
        var coolingParameters = new double[]{1e-3, 1e-2, 1e-1};
        var epochLengths = new int[]{1, 5, 10, 15, 20};
        var seeds = new int[]{0, 1, 2};

        var saKPIs = new LinkedList<>();

        for (var problem : problems) {
            for (var seed : seeds) {
                for (var temperatur : temperatures) {
                    for (var coolingParameter : coolingParameters) {
                        for (var epochLength : epochLengths) {
                            System.out.println(seed + ", " + temperatur + ", " + coolingParameter + ", " + epochLength);
                            var saSetting = new SASetting(seed, temperatur, coolingParameter, "geometric", epochLength, "minT,1");

                            var saKPI = runSA(problem, saSetting);
                            saKPIs.add(saKPI);
                        }
                    }
                }
            }
        }

        toJson(saKPIs, key + "_saKPIs.json");
    }

    private static SimulatedAnnealingKPI runSA(ProblemData problemData, SASetting saSetting) throws Exception {
        var greedyHeury = new GreedyHeuristic(problemData);
        var initialSolution = greedyHeury.solution;

        var simulatedAnnealing = new SimulatedAnnealing(problemData, saSetting.initialTemperature(),
                saSetting.coolingParameter(), saSetting.coolingMethod(), saSetting.epochLength(),
                saSetting.terminationCriteria(), initialSolution, saSetting.seed());

        simulatedAnnealing.run(0, 0);

        return simulatedAnnealing.kpi;
    }

    private static TabuKPI runTabuSearch(ProblemData problemData, TabuSetting tabuSetting){
        System.out.println("-----------------------------------------------------------------");

        var greedyHeury = new GreedyHeuristic(problemData);
        var initialSolution = greedyHeury.solution;

        var tabuSearch = new TabuSearch(problemData, tabuSetting, initialSolution);
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

    private static void toJson(List<Object> kpis, String fileName) throws IOException, IOException {
        try (var writer = new JsonWriter(new FileWriter(fileName))) {
            var gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(kpis, new TypeToken<List<Object>>(){}.getType(), writer);
        }
    }

}
