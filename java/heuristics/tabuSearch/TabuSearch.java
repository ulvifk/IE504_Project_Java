package heuristics.tabuSearch;

import data.ProblemData;
import heuristics.Neighbor;
import heuristics.neighborhoodSearch.InterSwapSearch;
import heuristics.neighborhoodSearch.IntraSwapSearch;
import heuristics.neighborhoodSearch.NDeepSearh;
import heuristics.neighborhoodSearch.TransferSearch;
import heuristics.neighborhoodSearch.moves.IMove;
import heuristics.Solution;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TabuSearch {
    private final ProblemData problemData;
    private final int tabuTenure;
    private Solution currentState;
    public Solution bestSolution;
    private List<Tabu> tabuList;
    private List<Solution> bestSolutions;
    public TabuKPI tabuKPI;
    private List<TabuIterationKPI> iterationKPIs;
    private int noImprovementIterationCount = 0;
    private boolean isDiversified = false;

    public TabuSearch(ProblemData problemData, Solution initialSolution, int tabuTenure) {
        this.problemData = problemData;
        this.tabuTenure = tabuTenure;
        this.currentState = initialSolution;
        this.bestSolution = initialSolution;
        this.tabuList = new LinkedList<>();
        this.bestSolutions = new LinkedList<>();

        this.iterationKPIs = new LinkedList<>();
    }

    public void solve(int maxIteration){
        int iteration = 0;
        while (iteration < maxIteration) {
            var neighbors = getNeighbors(this.currentState);
            var admissibleNeighbors = neighbors.stream()
                    .filter(neighbor -> isAdmissible(neighbor.solution(), neighbor.move())).toList();
            var bestNeighbor = admissibleNeighbors.stream().min(Neighbor::compareTo).orElse(null);


            if (bestNeighbor == null) {
                System.out.println("No admissible neighbor found");
                break;
            }

            if (isBetter(bestNeighbor.solution())) {
                this.bestSolution = bestNeighbor.solution();
                this.bestSolutions.add(this.bestSolution);

                System.out.println("Iteration: " + iteration + " Cost: " + this.bestSolution.objective);
                noImprovementIterationCount = 0;
            }

            if (noImprovementIterationCount > 100 && !isDiversified) {
                System.out.println("Diversifying");
                var oldObjective = currentState.objective;
                currentState = Diversification.divesify(currentState);
                System.out.println("Objective changed from " + oldObjective + " to " + currentState.objective);
                noImprovementIterationCount = 0;
                isDiversified = true;

            }

            updateTabuList(bestNeighbor);
            this.currentState = bestNeighbor.solution();

            TabuIterationKPI iterationKPI = new TabuIterationKPI(iteration, this.bestSolution.objective, this.currentState.objective);
            this.iterationKPIs.add(iterationKPI);

            noImprovementIterationCount++;
            iteration++;
            //System.out.println("Iteration: " + iteration + " Cost: " + this.bestSolution.objective);
        }

        var beforeIntensificationObjective = this.bestSolution.objective;

        System.out.println("Starting NDeepSearch");
        var bestSols = this.bestSolutions.stream().sorted().limit(10).collect(Collectors.toList());
        for (var sol : bestSols) {
            //var nDeepSearch = new NDeepSearh(sol, 2, 10 );
//            if (nDeepSearch.bestSolution.objective < this.bestSolution.objective) {
//                this.bestSolution = nDeepSearch.bestSolution;
//                System.out.println("NDeepSearch found better solution: " + this.bestSolution.objective);
//            }
        }

        this.tabuKPI = new TabuKPI(
                this.problemData.instanceName,
                this.tabuTenure,
                maxIteration,
                beforeIntensificationObjective,
                this.bestSolution.objective,
                this.iterationKPIs
        );
    }

    private List<Neighbor> getNeighbors(Solution solution) {
        var randInt = (int) (Math.random() * 3);

        if (randInt == 0) {
            return new InterSwapSearch(solution).neighbors;
        }
        if (randInt == 1) {
            return new IntraSwapSearch(solution).neighbors;
        }
        if (randInt == 2) {
            return new TransferSearch(solution).neighbors;
        }
        var interSwapSearch = new InterSwapSearch(solution);
        var intraSwapSearch = new IntraSwapSearch(solution);
        var transferSearch = new TransferSearch(solution);

        var neighbors = new LinkedList<Neighbor>();
        neighbors.addAll(interSwapSearch.neighbors);
        neighbors.addAll(intraSwapSearch.neighbors);
        neighbors.addAll(transferSearch.neighbors);

        return neighbors;
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
