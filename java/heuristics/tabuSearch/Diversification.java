package heuristics.tabuSearch;

import data.Customer;
import data.INode;
import data.Truck;
import heuristics.Solution;

import java.util.LinkedList;
import java.util.Random;

public class Diversification {
    private static Random random = new Random();
    public static Solution divesify(Solution solution){
        var newSolution = solution.copy();

        var truckIdx = random.nextInt(newSolution.routes.size());
        var truck = newSolution.routes.keySet().stream().toList().get(truckIdx);

        for (var node : newSolution.routes.get(truck)){
            if (!(node instanceof Customer customer)) continue;

            var newTruck = new Truck(truck.id(), truck.capacity(), truck.batteryCapacity(), truck.costPerKm());
            var newRoute = new LinkedList<INode>();
            newRoute.add(customer);

            newSolution.routes.put(newTruck, newRoute);
            newSolution.resourceCache.put(newTruck, new LinkedList<>());
        }

        newSolution.routes.remove(truck);
        newSolution.resourceCache.remove(truck);

        newSolution.update();

        return newSolution;
    }
}
