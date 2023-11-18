package heuristics.neighborhoodSearch;

import heuristics.Neighbor;
import heuristics.Solution;
import heuristics.neighborhoodSearch.moves.IMove;
import heuristics.neighborhoodSearch.moves.IntraSwapMove;

import java.util.Collections;


public class IntraSwapSearch extends BaseNeighborhoodSearch {

    public IntraSwapSearch(Solution solution) {
        super(solution);
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
                    if (!newSolution.isFeasible()) continue;

                    this.neighbors.add(new Neighbor(newSolution, move));
                }
            }
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
        newSolution.update();
        return newSolution;
    }
}
