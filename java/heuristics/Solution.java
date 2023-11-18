package heuristics;

import data.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Solution implements Comparable<Solution> {
    public Map<Truck, List<INode>> routes;
    public Map<Truck, List<TruckResourceCache>> resourceCache;
    public double objective;
    public final Customer depot;

    public Solution(Map<Truck, List<INode>> routes, Customer depot) {
        this.routes = routes;
        this.depot = depot;
        this.resourceCache = new HashMap<>();

        update();
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

    public Solution copy(){
        Map<Truck, List<INode>> clonedRoutes = this.routes.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, kvp -> new LinkedList<>(kvp.getValue())));

        return new Solution(clonedRoutes, this.depot);
    }

    private void calculateResourceCache(){
        this.routes.keySet().forEach(truck -> this.resourceCache.put(truck, new LinkedList<>()));

        for (var kvp : this.routes.entrySet()) {
            var truck = kvp.getKey();
            var route = kvp.getValue();

            double batteryLevel = truck.batteryCapacity();
            double remainingCapacity = truck.capacity();
            INode currentNode = this.depot;
            for (var node : route) {
                if (node instanceof Customer customer){
                    var consumption = truck.batteryConsumption(currentNode.distanceTo(customer));
                    batteryLevel -= consumption;
                    remainingCapacity -= customer.demand();
                }
                else {
                    batteryLevel = truck.batteryCapacity();
                }

                this.resourceCache.get(truck)
                        .add(new TruckResourceCache(truck, node, batteryLevel, remainingCapacity));
                currentNode = node;
            }
        }
    }

    public boolean isFeasible(){
        return checkBatteryFeasibility() && checkCapacityFeasibility();
    }

    private boolean checkBatteryFeasibility(){
        return resourceCache.values().stream()
                .flatMap(List::stream)
                .anyMatch(cache -> cache.batteryAtNode() < 0);
    }

    private boolean checkCapacityFeasibility(){
        return resourceCache.values().stream()
                .flatMap(List::stream)
                .anyMatch(cache -> cache.capacityAfterServingNode() < 0);
    }

    public void update(){
        calculateResourceCache();
        calculateObjective();
    }
}
