package heuristics;


import data.*;

import java.util.*;

public class GreedyHeuristic {
    private ProblemData problemData;
    private Map<Truck, List<INode>> routes;
    public Solution solution;


    public GreedyHeuristic(ProblemData problemData) {
        this.problemData = problemData;
        this.routes = new HashMap<>();

        run();
    }

    public void run() {
        var unvisitedCustomers = new LinkedList<>(problemData.customers);

        int truckId = 0;
        while (!unvisitedCustomers.isEmpty()) {
            var truck = new Truck(truckId++, problemData.truckParameters.truckCapacity, problemData.truckParameters.truckMaxTravelDistance, problemData.truckParameters.truckCostPerKm);
            var route = constructRouteForTruck(truck, unvisitedCustomers);
            routes.put(truck, route);
        }

        this.solution = new Solution(routes, problemData.depot);
    }

    private List<INode> constructRouteForTruck(Truck truck, List<Customer> unvisitedCustomers) {
        var remainingCapacity = truck.capacity();
        var currentBattery = truck.batteryCapacity();

        var route = new LinkedList<INode>();

        var minDemand = unvisitedCustomers.stream().mapToDouble(Customer::demand).min().orElse(0);

        INode prevNode = problemData.depot;
        while (remainingCapacity >= minDemand) {
            INode nextNode = findClosestNodeWithSmallEnoughDemand(
                    prevNode,
                    unvisitedCustomers,
                    truck,
                    remainingCapacity,
                    currentBattery
            );

            // If there is no node that can be visited, go to the closest station
            if (nextNode == null) {
                nextNode = getClosestStation(prevNode);
                currentBattery = truck.batteryCapacity();
            } else {
                unvisitedCustomers.remove(nextNode);
                remainingCapacity -= ((Customer) nextNode).demand();
                currentBattery -= truck.batteryConsumption(prevNode.distanceTo(nextNode));
            }

            route.add(nextNode);
            prevNode = nextNode;
            minDemand = unvisitedCustomers.stream().mapToDouble(Customer::demand).min().orElse(10000);
        }

        if (!doesSurvive(route.get(route.size() - 1), problemData.depot, truck, currentBattery)) {
            route.add(getClosestStation(route.get(route.size() - 1)));
        }

        return route;
    }

    private Customer findClosestNodeWithSmallEnoughDemand(INode node, List<Customer> nodes, Truck truck, double remainingCapacity, double remainingBattery) {
        return nodes.stream()
                .filter(n -> n != node)
                .filter(n -> n.demand() <= remainingCapacity)
                .min((n1, n2) -> Double.compare(node.distanceTo(n1), node.distanceTo(n2)))
                .filter(n -> doesSurvive(n, getClosestStation(n), truck, remainingBattery))
                .orElse(null);
    }

    private Station getClosestStation(INode node) {
        return problemData.stations.stream()
                .min(Comparator.comparingDouble(node::distanceTo))
                .orElse(null);
    }

    private boolean doesSurvive(INode from, INode to, Truck truck, double remainingBattery) {
        var distance = from.distanceTo(to);
        var batteryConsumption = truck.batteryConsumption(distance);
        return remainingBattery - batteryConsumption >= 0;
    }
}
