    import heuristics.Solution;
    import data.INode;
    import data.Truck;

    import java.util.List;
    import java.util.Map;
    import java.util.Random;
    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.HashMap;

    import java.time.Duration;
    import java.time.Instant;

    public class SimulatedAnnealing {

        double initialTemperature;
        double coolingParameter;
        int epochLength;
        String coolingMethod; // geometric, 
        Solution initialSolution;
        Solution incumbentSolution;
        String terminationCriteria; // minT, 100 or maxIt, 1000 etc.
        private Random random = new Random();

        public SimulatedAnnealing(double initialTemperature, double coolingParameter, String coolingMethod, int epochLength,String terminationCriteria, Solution initialSolution) {
            this.initialTemperature = initialTemperature;
            this.coolingParameter = coolingParameter;
            this.coolingMethod = coolingMethod;
            this.epochLength = epochLength;
            this.terminationCriteria = terminationCriteria;
            this.initialSolution = initialSolution;
            this.incumbentSolution = initialSolution;
        }


        public Solution run() throws Exception{
            double temperature = this.initialTemperature;
            int iterNumber = 0;
            Solution currentSolution = this.initialSolution;
            Solution incumbentSolution = this.incumbentSolution;
            while (!checkTerminationCriteria(this.terminationCriteria, temperature, iterNumber)){
                Solution neighborSolution = generateNeighbor("scramble", currentSolution);
                if (acceptanceCriteria(neighborSolution, currentSolution, temperature)) {
                    currentSolution = neighborSolution;
                    if (neighborSolution.compareTo(incumbentSolution) < 0) {
                        incumbentSolution = neighborSolution.copy();
                    }
                }
                temperature = updateTemperature(temperature, this.coolingMethod, this.coolingParameter);
                iterNumber++;
            }

            return incumbentSolution;
        }

        public boolean acceptanceCriteria(Solution currentSolution, Solution newSolution, double temperature) {
            double currentObjective = currentSolution.objective;
            double newObjective = newSolution.objective;
            if (newObjective < currentObjective) {
                return true;
            } else {
                double acceptanceProbability = Math.exp(-(newObjective - currentObjective) / temperature);
                double randomThreshold = random.nextDouble();
                return acceptanceProbability > randomThreshold;
            }
        }

        private double updateTemperature(double temperature, String coolingMethod, double parameter){
            if ("geometric".equals(coolingMethod)){
                return temperature * parameter;
            } else {
                return -1;
            }
        }

        private Solution generateNeighbor(String neighborhoodMove, Solution initSolution) throws Exception{
            // inspired from https://www.sciencedirect.com/science/article/pii/S2210650221000729
            Solution initSol = initSolution;
            if ("scramble".equals(neighborhoodMove)){
                    Solution neighborSolution = null;
                    boolean isFeasible = false;

                    Instant startTime = Instant.now();

                    while(!isFeasible) {
                        List<Truck> truckList = new ArrayList<>(initSol.routes.keySet());
                        Collections.shuffle(truckList);
                        Truck randomTruck = truckList.get(0);
                        List<INode> route = new ArrayList<>(initSol.routes.get(randomTruck));

                        int routeSize = route.size();
                        if (routeSize < 2) continue;

                        int subrouteStart = random.nextInt(routeSize-1);
                        int subrouteEnd = subrouteStart + random.nextInt(routeSize - subrouteStart);

                        Collections.shuffle(route.subList(subrouteStart, subrouteEnd + 1));

                        Map<Truck, List<INode>> newRoutes = new HashMap<>(initSol.routes);
                        newRoutes.put(randomTruck, route);
                        neighborSolution = new Solution(newRoutes, initSol.depot);

                        isFeasible = neighborSolution.isFeasible();
                        
                        if (Duration.between(startTime, Instant.now()).toMinutes() >= 5) {
                            throw new Exception("No feasible neighbor found within 5 minutes.");
                        }
                    }

                    return neighborSolution;

            } else{
                throw new Exception("Please give one of these moves: scramble,");
            }
        }

        private Boolean checkTerminationCriteria(String terminationCriteria, double T, int i){
            String[] criteriaParts = this.terminationCriteria.split(",");
            if (criteriaParts.length != 2) {
                throw new IllegalArgumentException("Invalid termination criteria format, please use minT,value or maxIt,value.");
            }
            String criteriaName = criteriaParts[0].trim();
            int value = Integer.parseInt(criteriaParts[1].trim());
            if ("minT".equals(criteriaName)){ // stop when we reach a minimum temperature
                return T>value;
            } else if ("maxIt".equals(criteriaName)){ // stop when we reach a number of iterations 
                return i<value;
            } else { // invalid termination criteria
                throw new IllegalArgumentException("Invalid termination criteria format, please use minT,value or maxIt,value."); 
            }
        }

        


    }
