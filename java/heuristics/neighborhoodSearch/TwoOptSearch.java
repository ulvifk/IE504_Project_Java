package heuristics.neighborhoodSearch;

import data.INode;
import data.ProblemData;
import data.Truck;
import heuristics.Neighbor;
import heuristics.Solution;
import heuristics.neighborhoodSearch.moves.IMove;
import heuristics.neighborhoodSearch.moves.TwoOptMove;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TwoOptSearch{
    private Solution solution;
    public List<Neighbor> neighbors;

    public TwoOptSearch(Solution solution) {
        this.solution = solution;
        SearchNeighbors();
    }

    protected void SearchNeighbors(){
        var neighbors = new LinkedList<Neighbor>();
        neighbors.add(new Neighbor(this.solution, null));
        var nextNeighbors = new LinkedList<Neighbor>();
        for (var kvp : this.solution.routes.entrySet()) {
            for (var neighbor : neighbors) {
                var sol = neighbor.solution();
                var truck = kvp.getKey();
                var route = sol.routes.get(truck);

                nextNeighbors.addAll(twoOpt(solution, truck));
            };
            neighbors = nextNeighbors;
            nextNeighbors = new LinkedList<Neighbor>();
        }
        this.neighbors = neighbors;
    }

    private List<Neighbor> twoOpt(Solution solution, Truck truck){
        var route = solution.routes.get(truck);

        var neighbors = new LinkedList<Neighbor>();
        for (int i = 0; i < route.size(); i++) {
            for (int j = i + 1; j < route.size(); j++) {
                var move = new TwoOptMove(truck, i, j);
                var newSolution = applyMove(move, solution);

                if (!newSolution.isFeasible) continue;

                var neighbor = new Neighbor(newSolution, move);
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    private void applyTwoOpt(List<INode> route, int i, int j){
        var newRoute = new LinkedList<INode>();
        newRoute.addAll(route.subList(0, i));
        var subRoute = route.subList(i, j);
        Collections.reverse(subRoute);
        newRoute.addAll(subRoute);
        newRoute.addAll(route.subList(j, route.size()));
    }


    Solution applyMove(IMove _move, Solution solution) {
        var newSolution = solution.copy();

        var move = (TwoOptMove) _move;
        var truck = move.truck();
        var route = newSolution.routes.get(truck);
        var i = move.i();
        var j = move.j();

        applyTwoOpt(route, i, j);
        newSolution.update(truck);

        return newSolution;
    }
}
