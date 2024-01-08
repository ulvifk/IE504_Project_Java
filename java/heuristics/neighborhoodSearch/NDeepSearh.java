package heuristics.neighborhoodSearch;

import heuristics.Neighbor;
import heuristics.Solution;
import heuristics.neighborhoodSearch.moves.IMove;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class NDeepSearh{
    private Solution solution;
    public Solution bestSolution;
    private int level;
    private int searchOverBestN;
    private boolean isRandomized = true;
    private Random random;

    public NDeepSearh(Solution solution, int level, int searchOverBestN, int seed) {
        this.solution = solution;
        this.bestSolution = solution;
        this.level = level;
        this.searchOverBestN = searchOverBestN;
        this.random = new Random(seed);
        searchNeighbors();
    }

    private void searchNeighbors(){
        List<Solution> previousLevelSolutions = new LinkedList<Solution>();
        List<Solution> currentLevelSolutions = new LinkedList<Solution>();
        var allSolutions = new LinkedList<Solution>();
        previousLevelSolutions.add(this.solution);
        allSolutions.add(this.solution);

        for (int i = 0; i<level; i++){
            for (var sol : previousLevelSolutions) {
                var neighbors = getNeighbors(sol);
                var solutions = new LinkedList<Solution>(neighbors.stream().map(Neighbor::solution).toList());
                currentLevelSolutions.addAll(neighbors.stream().map(Neighbor::solution).toList());
                allSolutions.addAll(solutions);
            }
            previousLevelSolutions = currentLevelSolutions;
            previousLevelSolutions = previousLevelSolutions.stream()
                    .sorted(Comparator.comparing(sol -> sol.objective))
                    .limit(searchOverBestN).toList();
            currentLevelSolutions = new LinkedList<Solution>();
        }

        this.bestSolution = allSolutions.stream().min(Comparator.comparing(sol -> sol.objective)).orElse(null);
    }

    private List<Neighbor> getNeighbors(Solution solution) {
        var randInt = this.random.nextInt(0, 3);

        if (randInt == 0) {
            return new TransferSearch(solution, false, 0).neighbors;
        }
        if (randInt == 1) {
            return new InterSwapSearch(solution, false, 0).neighbors;
        } else {
            return new IntraSwapSearch(solution, false, 0).neighbors;
        }

    }
}
