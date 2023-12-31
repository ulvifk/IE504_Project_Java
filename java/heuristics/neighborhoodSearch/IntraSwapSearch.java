package heuristics.neighborhoodSearch;

import heuristics.Neighbor;
import heuristics.Solution;
import heuristics.neighborhoodSearch.moves.IMove;
import heuristics.neighborhoodSearch.moves.IntraSwapMove;

import java.util.Collections;
import java.util.Random;


public class IntraSwapSearch extends BaseNeighborhoodSearch {
    private Random random;

    public IntraSwapSearch(Solution solution, boolean isRandom, int seed) {
        super(solution);
        this.random = new Random(seed);

        if (!isRandom) SearchNeighbors();
        else searchNeighborsRandom();
    }

    protected void SearchNeighbors() {
        for (var kvp : super.solution.routes.entrySet()) {
            var truck = kvp.getKey();
            var route = kvp.getValue();

            for (var node1 : route) {
                for (var node2 : route) {
                    if (node1 == node2) continue;

                    var move = new IntraSwapMove(truck, node1, node2);
                    var newSolution = applyMove(move);
                    if (!newSolution.isFeasible) continue;

                    this.neighbors.add(new Neighbor(newSolution, move));
                }
            }
        }
    }

    private void searchNeighborsRandom(){
        while (true){
            var truck = this.solution.routes.keySet().stream().toList().get(random.nextInt(this.solution.routes.size()));
            var route = this.solution.routes.get(truck);

            var node1 = route.get(random.nextInt(route.size()));
            var node2 = route.get(random.nextInt(route.size()));

            if (node1 == node2) continue;

            var move = new IntraSwapMove(truck, node1, node2);
            var newSolution = applyMove(move);
            if (!newSolution.isFeasible) continue;

            this.neighbors.add(new Neighbor(newSolution, move));
        }
    }

    protected Solution applyMove(IMove _move) {
        var move = (IntraSwapMove) _move;
        var newSolution = super.solution.copy();

        var truck = move.truck();
        var route = newSolution.routes.get(truck);

        var node1 = move.node1();
        var node2 = move.node2();

        Collections.swap(route, route.indexOf(node1), route.indexOf(node2));
        newSolution.update(truck);
        return newSolution;
    }
}
