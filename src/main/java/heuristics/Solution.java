package heuristics;

import data.INode;
import data.Node;
import data.Truck;

import java.util.List;
import java.util.Map;

public class Solution implements Comparable<Solution> {
    public Map<Truck, List<INode>> routes;
    public double objective;
    public final Node depot;

    public Solution(Map<Truck, List<INode>> routes, Node depot) {
        this.routes = routes;
        this.depot = depot;
        calculateObjective();
    }

    public void calculateObjective() {
        this.objective = this.routes.entrySet().stream()
                .mapToDouble(entry -> calculateRouteCost(entry.getKey(), entry.getValue()))
                .sum();
    }

    private double calculateRouteCost(Truck truck, List<INode> route) {
        double totalDistance = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            totalDistance += route.get(i).distanceTo(route.get(i + 1));
        }

        totalDistance += route.get(route.size() - 1).distanceTo(this.depot);
        totalDistance += this.depot.distanceTo(route.get(0));

        return totalDistance * truck.costPerKm();
    }

    @Override
    public int compareTo(Solution other) {
        return Double.compare(this.objective, other.objective);
    }
}
