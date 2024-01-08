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
import java.util.Random;
import java.util.stream.Collectors;

public class TabuSearch {
    private final ProblemData problemData;
    private final int tabuTenure;
    private Solution currentState;
    public Solution bestSolution;
    private List<Tabu> tabuList;
    private List<Solution> bestSolutions;
    public TabuKPI tabuKPI;
    private List<TabuIterationKPI> iterationKPIsBeforeDiversification;
    private List<TabuIterationKPI> iterationKPIsAfterDiversification;
    private List<TabuIterationKPI> iterationKPIs;
    private int noImprovementIterationCount = 0;
    private boolean isDiversified = false;
    private boolean isRandomized = true;
    private Random random;
    private TabuSetting tabuSetting;
    private int seed;

    public TabuSearch(ProblemData problemData, TabuSetting tabuSetting, Solution initialSolution) {
        this.problemData = problemData;
        this.tabuTenure = tabuSetting.tabuTenure();
        this.tabuSetting = tabuSetting;
        this.currentState = initialSolution;
        this.bestSolution = initialSolution;
        this.tabuList = new LinkedList<>();
        this.bestSolutions = new LinkedList<>();

        this.iterationKPIs = new LinkedList<>();
        this.iterationKPIsBeforeDiversification = new LinkedList<>();
        this.iterationKPIsAfterDiversification = new LinkedList<>();

        this.random = new Random(tabuSetting.seed());
        this.seed = tabuSetting.seed();
    }

    public void solve(int maxIteration){
        var start = System.currentTimeMillis();
        int iteration = 0;
        while (iteration < maxIteration) {
            var iterationStart = System.currentTimeMillis();

            var neighbors = getNeighbors(this.currentState);
            var admissibleNeighbors = neighbors.stream()
                    .filter(neighbor -> isAdmissible(neighbor.solution(), neighbor.move())).toList();
            var bestNeighbor = admissibleNeighbors.stream().min(Neighbor::compareTo).orElse(null);

            if (bestNeighbor == null) {
//                System.out.println("No admissible neighbor found");
                break;
            }

            this.currentState = bestNeighbor.solution();

            if (isBetter(bestNeighbor.solution())) {
                this.bestSolution = bestNeighbor.solution();
                this.bestSolutions.add(this.bestSolution);

//                System.out.println("Iteration: " + iteration + " Cost: " + this.bestSolution.objective);
                noImprovementIterationCount = 0;
            }

            if (noImprovementIterationCount > this.tabuSetting.diversificationSetting().nNumberOfNonImprovingIterations()
                    && !isDiversified) {
//                System.out.println("Diversifying");
                var oldObjective = currentState.objective;
                currentState = Diversification.divesify(currentState);
//                System.out.println("Objective changed from " + oldObjective + " to " + currentState.objective);
                noImprovementIterationCount = 0;
                isDiversified = true;
            }

            updateTabuList(bestNeighbor);

            var iterationEnd = System.currentTimeMillis();
            var iterationTime = iterationEnd - iterationStart;
            TabuIterationKPI iterationKPI = new TabuIterationKPI(iteration,
                    this.bestSolution.objective,
                    this.currentState.objective,
                    iterationTime);
            this.iterationKPIs.add(iterationKPI);

            if (isDiversified) this.iterationKPIsAfterDiversification.add(iterationKPI);
            else this.iterationKPIsBeforeDiversification.add(iterationKPI);

            noImprovementIterationCount++;
            iteration++;
            //System.out.println("Iteration: " + iteration + " Cost: " + this.currentState.objective + " Best: " + this.bestSolution.objective);

        }

        var beforeIntensificationObjective = this.bestSolution.objective;

        var intensificationStart = System.currentTimeMillis();
        System.out.println("Starting NDeepSearch");
        var bestSols = this.bestSolutions.stream().sorted().limit(this.tabuSetting.intensificationSetting().searchNBest()).toList();
        for (var sol : bestSols) {
            var nDeepSearch = new NDeepSearh(sol, this.tabuSetting.intensificationSetting().level(), this.tabuSetting.intensificationSetting().searchNNeighborhood(), this.seed);
            if (nDeepSearch.bestSolution.objective < this.bestSolution.objective) {
                this.bestSolution = nDeepSearch.bestSolution;
                System.out.println("NDeepSearch found better solution: " + this.bestSolution.objective);
            }
        }
        var intensificationEnd = System.currentTimeMillis();

        var end = System.currentTimeMillis();

        this.tabuKPI = new TabuKPI(
                this.problemData.instanceName,
                this.seed,
                this.tabuTenure,
                maxIteration,
                this.tabuSetting.intensificationSetting(),
                this.tabuSetting.diversificationSetting(),
                beforeIntensificationObjective,
                this.bestSolution.objective,
                this.iterationKPIs,
                this.iterationKPIsBeforeDiversification,
                this.iterationKPIsAfterDiversification,
                end-start,
                intensificationEnd-intensificationStart
        );
    }

    private List<Neighbor> getNeighbors(Solution solution) {
        var randInt = this.random.nextInt(0, 3);

        if (this.isRandomized){
            if (randInt == 0) {
                return new TransferSearch(solution, false, 0).neighbors;
            }
            if (randInt == 1) {
                return new InterSwapSearch(solution, false, 0).neighbors;
            }
            else {
                return new IntraSwapSearch(solution, false, 0).neighbors;
            }
        }

        var interSwapSearch = new InterSwapSearch(solution, false, 0);
        var intraSwapSearch = new IntraSwapSearch(solution, false, 0);
        var transferSearch = new TransferSearch(solution, false, 0);

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
