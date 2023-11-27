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
        String terminationCriteria; // minT, 0.1 or maxIt,1000 etc.
        private Random random = new Random();

        public SimulatedAnnealing(double initialTemperature, double coolingParameter, String coolingMethod, int epochLength,String terminationCriteria, Solution initialSolution) {
            this.initialTemperature = initialTemperature;
            this.coolingParameter = coolingParameter;
            this.coolingMethod = coolingMethod;
            this.epochLength = epochLength;
            this.terminationCriteria = terminationCriteria;
            this.initialSolution = initialSolution;
            this.incumbentSolution = initialSolution;
            System.out.println("Created the SA!");
        }


        public Solution run() throws Exception{
            System.out.println("Started run method...");
            double temperature = this.initialTemperature;
            int iterNumber = 0;
            Solution currentSolution = this.initialSolution;
            Solution incumbentSolution = this.incumbentSolution;
            initialSolution.calculateObjective();
            double initialObj = initialSolution.objective;
            System.out.println("Initial objective:"+ initialObj);
            while (!checkTerminationCriteria(this.terminationCriteria, temperature, iterNumber)){
                for(int i = 0; i<this.epochLength; i++){
                    incumbentSolution.calculateObjective();
                    double incumbentObj = incumbentSolution.objective;
                    System.out.println("Iteration:"+ iterNumber+ " epoch:"+ i + "--> T="+ temperature + ", incumbent solution:"+ incumbentObj);
                    Solution neighborSolution = generateNeighbor("scramble", currentSolution);
                    if (acceptanceCriteria(neighborSolution, currentSolution, temperature)) {
                        currentSolution = neighborSolution;
                        currentSolution.calculateObjective();
                        incumbentSolution.calculateObjective();
                        if (currentSolution.objective > incumbentSolution.objective) {
                            incumbentSolution = currentSolution.copy();
                        }
                    }
                }
                temperature = updateTemperature(temperature, this.coolingMethod, this.coolingParameter);
                iterNumber++;
            }
            incumbentSolution.calculateObjective();
            System.out.println("I am returning " + incumbentSolution.objective);
            return incumbentSolution;
        }

        public boolean acceptanceCriteria(Solution currentSolution, Solution newSolution, double temperature) {
            currentSolution.calculateObjective();
            newSolution.calculateObjective();
            double currentObjective = currentSolution.objective;
            double newObjective = newSolution.objective;
            System.out.println("    current obj:" + currentObjective + " neighbor obj:"+ newObjective);
            if (newObjective > currentObjective) {
                System.out.println("        accept the better solution");
                return true;
            } else {
                double acceptanceProbability = Math.exp(-(newObjective - currentObjective) / temperature);
                double randomThreshold = random.nextDouble();
                System.out.println("        accept p:"+ acceptanceProbability+", random treshold:" + randomThreshold);
                return acceptanceProbability > randomThreshold;
            }
        }
        
        private double updateTemperature(double temperature, String coolingMethod, double parameter){
            if ("geometric".equals(coolingMethod)){
                System.out.println("            I update the temperature :)");
                return temperature * (1-parameter);
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
                    System.out.println("    looking for new neighborhood solution...");
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
                    System.out.println("    found a new neighborhood solution!");
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
                return T<value;
            } else if ("maxIt".equals(criteriaName)){ // stop when we reach a number of iterations 
                return i>=value;
            } else { // invalid termination criteria
                throw new IllegalArgumentException("Invalid termination criteria format, please use minT,value or maxIt,value."); 
            }
        }

        


    }
