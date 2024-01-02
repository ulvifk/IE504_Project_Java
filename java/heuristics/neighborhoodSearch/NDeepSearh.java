package heuristics.neighborhoodSearch;

import heuristics.Neighbor;
import heuristics.Solution;
import heuristics.neighborhoodSearch.moves.IMove;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class NDeepSearh{
    private Solution solution;
    public Solution bestSolution;
    private int level;

    public NDeepSearh(Solution solution, int level) {
        this.solution = solution;
        this.bestSolution = solution;
        this.level = level;
        SearchNeighbors();
    }

    protected void SearchNeighbors() {
        var sol = SearchNeighbors(this.solution, 0);
        if (sol == null) return;
        if (sol.compareTo(this.bestSolution) < 0) {
            this.bestSolution = sol;
        }
    }

    private Solution SearchNeighbors(Solution solution, int level) {
        if (level == this.level) return null;

        var neighbors = getNeighbors(solution);
        var solutions = new LinkedList<Solution>(neighbors.stream().map(Neighbor::solution).toList());

        neighbors = neighbors.stream()
                .sorted(Comparator.comparing(neighbor -> neighbor.solution().objective))
                .limit(5).toList();
        for (var neighbor : neighbors) {
            var sol = SearchNeighbors(neighbor.solution(), level + 1);
            if (sol == null) sol = neighbor.solution();
            solutions.add(sol);
        }

        solutions.addAll(neighbors.stream().map(Neighbor::solution).toList());
        return solutions.stream().min(Solution::compareTo).orElse(null);
    }

    private List<Neighbor> getNeighbors(Solution solution) {
        var neighbors = new LinkedList<Neighbor>();
        var intraSwapSearch = new IntraSwapSearch(solution);
        var interSwapSearch = new InterSwapSearch(solution);
        var transferSearch = new TransferSearch(solution);

        neighbors.addAll(intraSwapSearch.neighbors);
        neighbors.addAll(interSwapSearch.neighbors);
        neighbors.addAll(transferSearch.neighbors);

        return neighbors;
    }
}
