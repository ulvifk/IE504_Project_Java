import heuristics.Solution;
import data.INode;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;

public class SimulatedAnnealing {

    double initialTemperature;
    double coolingRate;
    int epochLength;
    String coolingMethod;
    Solution initialSolution;
    Solution incumbentSolution;
    String terminationCriteria; // minT, 100 or maxIt, 1000 etc.

    public SimulatedAnnealing(double initialTemperature, double coolingRate, String coolingMethod, int epochLength,String terminationCriteria, Solution initialSolution) {
        this.initialTemperature = initialTemperature;
        this.coolingRate = coolingRate;
        this.coolingMethod = coolingMethod;
        this.epochLength = epochLength;
        this.terminationCriteria = terminationCriteria;
        this.initialSolution = initialSolution;
        this.incumbentSolution = initialSolution;
    }


    public Solution run(){
        double temperature = this.initialTemperature;
        int iterNumber = 0;
        
        while (checkTerminationCriteria(this.terminationCriteria, temperature, iterNumber)){
            
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
            return i<value;
        } else { // invalid termination criteria
            throw new IllegalArgumentException("Invalid termination criteria format, please use minT,value or maxIt,value.");
            return false;
        }
    }
    private double geometricTemperatureChange(double T, double alpha){
        return T*alpha;
    }


    private Random random = new Random();


}
