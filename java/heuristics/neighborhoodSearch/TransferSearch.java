package heuristics.neighborhoodSearch;

import heuristics.Neighbor;
import heuristics.Solution;
import heuristics.neighborhoodSearch.moves.IMove;
import heuristics.neighborhoodSearch.moves.TransferMove;


public class TransferSearch extends BaseNeighborhoodSearch {

    public TransferSearch(Solution solution) {
        super(solution);
        SearchNeighbors();
    }

    protected void SearchNeighbors() {
        for (var kvp : super.solution.routes.entrySet()) {
            var truck1 = kvp.getKey();
            var route1 = kvp.getValue();
            for (var node : route1){
                for (var kvp2 : super.solution.routes.entrySet()) {
                    var truck2 = kvp2.getKey();
                    var route2 = kvp2.getValue();
                    if (truck1 == truck2) continue;

                    for (int i = 0; i < route2.size() + 1; i++) {
                        var move = new TransferMove(truck1, truck2, node, i);
                        var newSolution = applyMove(move);
                        if (!newSolution.isFeasible) continue;

                        this.neighbors.add(new Neighbor(newSolution, move));
                    }
                }
            }
        }
    }

    protected Solution applyMove(IMove _move) {
        var move = (TransferMove) _move;
        var newSolution = super.solution.copy();

        var fromTruck = move.fromTruck();
        var toTruck = move.toTruck();
        var node = move.node();
        var toIndex = move.toIndex();

        newSolution.routes.get(fromTruck).remove(node);
        newSolution.routes.get(toTruck).add(toIndex, node);

        newSolution.update(fromTruck, toTruck);
        return newSolution;
    }
}
