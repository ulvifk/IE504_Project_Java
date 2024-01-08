package heuristics.neighborhoodSearch;

import heuristics.Neighbor;
import heuristics.Solution;
import heuristics.neighborhoodSearch.moves.IMove;
import heuristics.neighborhoodSearch.moves.TransferMove;

import java.util.Collections;
import java.util.Random;


public class TransferSearch extends BaseNeighborhoodSearch {
    private Random random;

    public TransferSearch(Solution solution, boolean isRandom, int seed) {
        super(solution);

        if (!isRandom) SearchNeighbors();
        else searchNeighborsRandom();
        this.random = new Random(seed);
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

    private void searchNeighborsRandom(){
        while (true){
            var truck1 = this.solution.routes.keySet().stream().toList().get(random.nextInt(this.solution.routes.size()));
            var route1 = this.solution.routes.get(truck1);

            var truck2 = this.solution.routes.keySet().stream().toList().get(random.nextInt(this.solution.routes.size()));
            var route2 = this.solution.routes.get(truck2);

            var node = route1.get(random.nextInt(route1.size()));

            var i = random.nextInt(route2.size() + 1);

            if (truck1 == truck2) continue;

            var move = new TransferMove(truck1, truck2, node, i);
            var newSolution = applyMove(move);
            if (!newSolution.isFeasible) continue;

            this.neighbors.add(new Neighbor(newSolution, move));
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

        if (newSolution.routes.get(fromTruck).isEmpty()) {
            newSolution.routes.remove(fromTruck);
            newSolution.update(toTruck);
        }
        else {
            newSolution.update(fromTruck, toTruck);
        }
        return newSolution;
    }
}
