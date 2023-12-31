package heuristics.neighborhoodSearch;

import heuristics.Neighbor;
import heuristics.Solution;
import heuristics.neighborhoodSearch.moves.IMove;
import heuristics.neighborhoodSearch.moves.InterSwapMove;

import java.util.Collections;
import java.util.Random;


public class InterSwapSearch extends BaseNeighborhoodSearch {
    private Random random;

    public InterSwapSearch(Solution solution, boolean isRandom, int seed) {
        super(solution);
        this.random = new Random(seed);

        if (!isRandom) SearchNeighbors();
        else searchNeighborsRandom();
    }

    protected void SearchNeighbors() {
        for (var kvp : super.solution.routes.entrySet()) {
            var truck1 = kvp.getKey();
            var route = kvp.getValue();

            for (var node1 : route) {
                for (var truck2 : super.solution.routes.keySet()) {
                    if (truck1 == truck2) continue;

                    var route2 = super.solution.routes.get(truck2);
                    for (var node2 : route2) {
                        var move = new InterSwapMove(truck1, truck2, node1, node2);
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
            var route = this.solution.routes.get(truck1);

            var node1 = route.get(random.nextInt(route.size()));

            var truck2 = this.solution.routes.keySet().stream().toList().get(random.nextInt(this.solution.routes.size()));
            var route2 = this.solution.routes.get(truck2);

            var node2 = route2.get(random.nextInt(route2.size()));

            if (truck1 == truck2) continue;

            var move = new InterSwapMove(truck1, truck2, node1, node2);
            var newSolution = applyMove(move);
            if (!newSolution.isFeasible) continue;

            this.neighbors.add(new Neighbor(newSolution, move));
        }
    }

    protected Solution applyMove(IMove _move) {
        var move = (InterSwapMove) _move;
        var newSolution = super.solution.copy();

        var truck1 = move.truck1();
        var truck2 = move.truck2();

        var node1 = move.node1();
        var node2 = move.node2();

        var route1 = newSolution.routes.get(truck1);
        var route2 = newSolution.routes.get(truck2);

        var node1Index = route1.indexOf(node1);
        var node2Index = route2.indexOf(node2);

        route1.remove(node1Index);
        route2.remove(node2Index);

        route1.add(node1Index, node2);
        route2.add(node2Index, node1);

        newSolution.update(truck1, truck2);
        return newSolution;
    }
}

