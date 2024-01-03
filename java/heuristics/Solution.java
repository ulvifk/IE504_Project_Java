package heuristics;

import data.*;
import java.util.*;

public class Solution implements Comparable<Solution> {
    public Map<Truck, List<INode>> routes;
    public Map<Truck, List<TruckResourceCache>> resourceCache;
    public double objective;
    public final Customer depot;
    public boolean isFeasible;

    public Solution(Map<Truck, List<INode>> routes, Customer depot) {
        this.routes = routes;
        this.depot = depot;
        this.resourceCache = new LinkedHashMap<>();

        update();
    }

    public Solution(Map<Truck, List<INode>> routes, Map<Truck, List<TruckResourceCache>> resourceCache, Customer depot, boolean isFeasible, double objective) {
        this.routes = routes;
        this.depot = depot;
        this.resourceCache = new LinkedHashMap<>();
        
        this.isFeasible = isFeasible;
        this.objective = objective;
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

        Map<Truck, List<INode>> clonedRoutes = new LinkedHashMap<>();
        Map<Truck, List<TruckResourceCache>> clonedResourceCache = new LinkedHashMap<>();

        for (var kvp : this.routes.entrySet()) {
            var truck = kvp.getKey();
            var route = kvp.getValue();
            clonedRoutes.put(truck, new LinkedList<>(route));
        }

        for (var kvp : this.resourceCache.entrySet()) {
            var truck = kvp.getKey();
            var resourceCache = kvp.getValue();

            var clonedCache = new LinkedList<TruckResourceCache>();
            for (var cache : resourceCache) {
                clonedCache.add(cache.clone());
            }
            clonedResourceCache.put(truck, clonedCache);
        }

        return new Solution(clonedRoutes, resourceCache, this.depot, this.isFeasible, this.objective);
    }

    private void calculateResourceCache(Truck truck){
        var route = this.routes.get(truck);
        var resourceCache = this.resourceCache.get(truck);

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

            resourceCache.add(new TruckResourceCache(truck, node, batteryLevel, remainingCapacity));
            currentNode = node;
        }
    }

    private void calculateResourceCache(){
        for (var truck : this.routes.keySet()) {
            this.resourceCache.put(truck, new LinkedList<>());
            calculateResourceCache(truck);
        }
    }
    private void calculateResourceCache(Truck... trucks){
        for (var truck : trucks) {
            this.resourceCache.put(truck, new LinkedList<>());
            calculateResourceCache(truck);
        }
    }

    private boolean isFeasible(){
        return this.routes.keySet().stream()
                .allMatch(this::isFeasible);
    }

    private boolean isFeasible(Truck... truck){
        return List.of(truck).stream()
                .allMatch(this::isFeasible);
    }

    private boolean isFeasible(Truck truck){
        return resourceCache.get(truck).stream()
                .allMatch(cache -> cache.batteryAtNode() >= 0 && cache.capacityAfterServingNode() >= 0);
    }

    public void update(){
        calculateResourceCache();
        calculateObjective();
        this.isFeasible = isFeasible();
    }
    public void update(Truck... trucks){
        calculateResourceCache(trucks);
        calculateObjective();
        this.isFeasible = isFeasible(trucks);
    }
}
