package gurobiSolution;
import gurobi.*;
import heuristics.Solution;

import java.util.*;
import java.util.stream.Collectors;

import data.Customer;
import data.INode;
import data.ProblemData;
import data.Station;
import data.Truck;

public class ECVRPSolver {
    private ProblemData problemData;
    private GRBEnv environment;
    private GRBModel model;

    // variables
    private GRBVar[][][] x; // Vehicle arc traversing variable
    private GRBVar[][] y;   // Battery level variable
    private GRBVar[] d;     // Vehicle dispatch variable
    private GRBVar[][][] w; // Penalty variable for battery level

    public ECVRPSolver(ProblemData problemData) throws GRBException {
        //this.problemData = problemData;
        this.environment = new GRBEnv();
        this.model = new GRBModel(environment);
        this.model.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE);
        System.out.println("hey");
    }
    
    public Solution solve() throws GRBException {
        initializeVariables();
        setObjective();
        setConstraints();
        model.setCallback(new SubtourEliminationCallback(x, problemData.customers.size() + problemData.stations.size() + 1));
        model.optimize();

        return extractSolution();
    }

    private void initializeVariables() throws GRBException {
        int numCustomers = problemData.customers.size();
        int numStations = problemData.stations.size();
        int numVehicles = problemData.customers.size()+1; // 1 vehicle per customer at the very worst case
        int numNodes = numCustomers + numStations + 1; 

        x = new GRBVar[numVehicles][numNodes][numNodes];
        y = new GRBVar[numVehicles][numNodes];
        d = new GRBVar[numVehicles];
        w = new GRBVar[numVehicles][numNodes][numNodes];

        // x_ij
        for (int v = 0; v < numVehicles; v++) {
            for (int i = 0; i < numNodes; i++) {
                for (int j = 0; j < numNodes; j++) {
                    x[v][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x_" + v + "_" + i + "_" + j);
                }
            }
        }

        // y_ij
        for (int v = 0; v < numVehicles; v++) {
            for (int i = 0; i < numNodes; i++) {
                y[v][i] = model.addVar(0.0, problemData.truckParameters.truckCapacity, 0.0, GRB.CONTINUOUS, "y_" + v + "_" + i);
            }
        }

        // d_v
        for (int v = 0; v < numVehicles; v++) {
            d[v] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "d_" + v);
        }

        //w_ij
        for (int v = 0; v < numVehicles; v++) {
            for (int i = 0; i < numNodes; i++) {
                for (int j = 0; j < numNodes; j++) {
                    w[v][i][j] = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "w_" + v + "_" + i + "_" + j);
                }
            }
        }
        model.update();

    }

    private void setObjective() throws GRBException {
        GRBLinExpr objective = new GRBLinExpr();

        double dispatchCost = problemData.dispatchCost;

        for (int v = 0; v < x.length; v++) {
            // Iterate over all arcs
            for (int i = 0; i < x[v].length; i++) {
                for (int j = 0; j < x[v][i].length; j++) {
                    if (i != j) {
                        // Add the cost of traversing arc (i, j) if x[v][i][j] is 1
                        objective.addTerm(getDistance(i, j) * problemData.truckParameters.truckCostPerKm, x[v][i][j]);
                    }
                }

                // Add recharging cost if node i is a charging station
                if (i >= problemData.customers.size() && i < problemData.customers.size() + problemData.stations.size()) {
                    int stationIndex = i - problemData.customers.size();
                    Station station = problemData.stations.get(stationIndex);
                    objective.addTerm(station.getChargingRate(), y[v][i]);
                }
            }
            
            objective.addTerm(dispatchCost, d[v]);

        }
        model.setObjective(objective, GRB.MINIMIZE);
    
    }

    private double getDistance(int i, int j) {
        // Retrieve node objects from their indices
        INode nodeI = getNodeFromIndex(i);
        INode nodeJ = getNodeFromIndex(j);
        // Use the distanceTo method to calculate the distance between them
        return nodeI.distanceTo(nodeJ);
    }

    private INode getNodeFromIndex(int index) {
        if (index == 0) {
            return problemData.depot;
        }
        else if (index <= problemData.customers.size()) {
            return problemData.customers.get(index - 1);
        }
        else {
            return problemData.stations.get(index - problemData.customers.size() - 1);
        }
    }

    private void setConstraints() throws GRBException {
        int numNodes = problemData.customers.size() + problemData.stations.size() + 1;
        int numVehicles = d.length;

        // Capacity constraints for each vehicle
        for (int v = 0; v < numVehicles; v++) {
            GRBLinExpr vehicleCapacity = new GRBLinExpr();
            for (Customer customer : problemData.customers) {
                int i = customer.id(); 
                vehicleCapacity.addTerm(customer.demand(), y[v][i]);
            }
            String cname = "cap_" + v;
            model.addConstr(vehicleCapacity, GRB.LESS_EQUAL, problemData.truckParameters.truckCapacity, cname);
        }

        // Each customer is visited exactly once
        for (Customer customer : problemData.customers) {
            GRBLinExpr visitOnce = new GRBLinExpr();
            for (int v = 0; v < numVehicles; v++) {
                for (int j = 0; j < numNodes; j++) {
                    if (customer.id() != j) { 
                        visitOnce.addTerm(1.0, x[v][customer.id()][j]);
                    }
                }
            }
            String cname = "visit_" + customer.id();
            model.addConstr(visitOnce, GRB.EQUAL, 1, cname);
        }

        // Flow balance constraints for customers and stations
        for (int v = 0; v < numVehicles; v++) {
            for (int i = 1; i < numNodes; i++) { 
                GRBLinExpr inflow = new GRBLinExpr();
                GRBLinExpr outflow = new GRBLinExpr();
                for (int j = 0; j < numNodes; j++) {
                    if (i != j) {
                        inflow.addTerm(1.0, x[v][j][i]);
                        outflow.addTerm(1.0, x[v][i][j]);
                    }
                }
                String cname = "flow_" + v + "_" + i;
                model.addConstr(inflow, GRB.EQUAL, outflow, cname);
            }
        }

        // Battery level constraints for each vehicle at each customer
        for (int v = 0; v < numVehicles; v++) {
            for (int i = 0; i < numNodes; i++) {
                GRBLinExpr batteryUsage = new GRBLinExpr();
                for (int j = 0; j < numNodes; j++) {
                    if (i != j) {
                        double distance = getDistance(i, j);
                        batteryUsage.addTerm(distance, x[v][i][j]);
                    }
                }
                String cname = "battery_" + v + "_" + i;
                model.addConstr(batteryUsage, GRB.LESS_EQUAL, problemData.truckParameters.truckMaxTravelDistance, cname);
            }
        }

        // Dispatching constraints: if a vehicle is dispatched, it must leave the depot and return to it
        for (int v = 0; v < numVehicles; v++) {
            GRBLinExpr dispatchOut = new GRBLinExpr();
            GRBLinExpr dispatchIn = new GRBLinExpr();
            for (int j = 1; j < numNodes; j++) { 
                dispatchOut.addTerm(1.0, x[v][0][j]); // From depot to any node
                dispatchIn.addTerm(1.0, x[v][j][0]); // From any node to depot
            }
            String cnameOut = "dispatchOut_" + v;
            String cnameIn = "dispatchIn_" + v;
            model.addConstr(dispatchOut, GRB.EQUAL, d[v], cnameOut);
            model.addConstr(dispatchIn, GRB.EQUAL, d[v], cnameIn);
        }

        // Penalty for battery level dropping below 20%
        for (int v = 0; v < numVehicles; v++) {
            for (int i = 0; i < numNodes; i++) {
                GRBLinExpr batteryLevel = new GRBLinExpr();
                batteryLevel.addTerm(1.0, y[v][i]);
                String cname = "penalty_" + v + "_" + i;
                model.addConstr(batteryLevel, GRB.GREATER_EQUAL, 0.2 * problemData.truckParameters.truckCapacity, cname);
            }
        }
        
        // battery level constraint
        for (int v = 0; v < numVehicles; v++) {
            GRBLinExpr totalDistance = new GRBLinExpr();
            for (int i = 0; i < numNodes; i++) {
                for (int j = 0; j < numNodes; j++) {
                    if (i != j) {
                        totalDistance.addTerm(getDistance(i, j), x[v][i][j]);
                    }
                }
            }
            String cname = "max_distance_" + v;
            model.addConstr(totalDistance, GRB.LESS_EQUAL, problemData.truckParameters.truckMaxTravelDistance, cname);
        }




        model.update();
    }

    private Solution extractSolution() throws GRBException {
        // After optimization, extract the solution from the model
        // Construct the Solution object with the optimized routes
        Map<Truck, List<INode>> optimizedRoutes = new HashMap<>();
        // ... populate optimizedRoutes ...
        Customer depot = problemData.depot; 

        return new Solution(optimizedRoutes, depot);
    }

}
