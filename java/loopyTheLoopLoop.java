import data.ProblemData;
import heuristics.GreedyHeuristic;
import heuristics.Solution;
import heuristics.tabuSearch.TabuKPI;
import heuristics.tabuSearch.TabuSearch;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

public class loopyTheLoopLoop {
    public static void main(String[] args) throws FileNotFoundException {
        var problems = Main.readJsonFiles("./java/dataset");
        var tabuTenures = new int[]{20};

        var tabuKPIs = new LinkedList<TabuKPI>();
        var problemIndices = new int[]{0};

        for (var problemIndex : problemIndices) {
            for (var tabuTenure : tabuTenures) {
                var tabuSetting = new TabuSetting(tabuTenure, 100);
                var problem = problems.get(problemIndex);

                var tabuKPI = runTabuSearch(problem, tabuSetting);
                tabuKPIs.add(tabuKPI);
            }
        }

        writeTabuKPIsToCsv(tabuKPIs, "tabuKPIs.csv");
    }

    private static TabuKPI runTabuSearch(ProblemData problemData, TabuSetting tabuSetting){
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
}
