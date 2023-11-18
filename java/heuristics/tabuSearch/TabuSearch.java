package heuristics.tabuSearch;

import heuristics.Neighbor;
import heuristics.neighborhoodSearch.moves.IMove;
import heuristics.Solution;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TabuSearch {
    private int tabuTenure;
    private Solution currentState;
    public Solution bestSolution;
    private List<Tabu> tabuList;

    public TabuSearch(int tabuTenure, Solution initialSolution) {
        this.tabuTenure = tabuTenure;
        this.currentState = initialSolution;
        this.bestSolution = initialSolution;
        this.tabuList = new LinkedList<>();
    }

    public void solve(int maxIteration){
        int iteration = 0;
        while (iteration < maxIteration) {
            var neighbors = getNeighbors(this.currentState);
            var admissibleNeighbors = neighbors.stream().filter(neighbor -> isAdmissible(neighbor.solution(), neighbor.move())).toList();
            var bestNeighbor = admissibleNeighbors.stream().min(Neighbor::compareTo).orElse(null);

            if (bestNeighbor == null) {
                System.out.println("No admissible neighbor found");
                break;
            }

            if (isBetter(bestNeighbor.solution())) {
                this.bestSolution = bestNeighbor.solution();
                System.out.println("Iteration: " + iteration + " Cost: " + this.bestSolution.objective);
            }

            updateTabuList(bestNeighbor);
            this.currentState = bestNeighbor.solution();

            iteration++;
        }
    }

    private List<Neighbor> getNeighbors(Solution solution) {
        return null;
    }

    private boolean isAdmissible(Solution solution, IMove move) {
        return !isTabu(move) || isBetter(solution);
    }

    private boolean isTabu(IMove move) {
        return this.tabuList.stream().anyMatch(tabu -> tabu.move.equals(move));
    }

    private boolean isBetter(Solution solution) {
        return solution.compareTo(this.bestSolution) < 0;
    }

    private void updateTabuList(Neighbor neighbor) {
        Tabu newTabu = new Tabu(neighbor.move(), tabuTenure);

        this.tabuList.forEach(tabu -> tabu.remainingIteration--);
        this.tabuList = this.tabuList.stream()
                .filter(tabu -> tabu.remainingIteration > 0)
                .collect(Collectors.toList());
        this.tabuList.add(newTabu);
    }
}
