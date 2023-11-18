import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import data.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }


    public static List<ProblemData> readJsonFiles(String directoryPath) {
        List<ProblemData> instances = new ArrayList<>();
        File folder = new File(directoryPath);
        File[] listOfFiles = folder.listFiles();
        JSONParser parser = new JSONParser();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    System.out.println("on next file");
                    try (FileReader reader = new FileReader(file)) {
                        JSONObject jsonObject = (JSONObject) parser.parse(reader);
                        ProblemData problemData = createProblemDataFromJSON(jsonObject);
                        instances.add(problemData);
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else {
            System.out.println("Empty directory");
        }
        return instances;
    }

    private static ProblemData createProblemDataFromJSON(JSONObject jsonObject) {
        // read json files from the dataset folder and create problem instances
        // OpenAI. (2023). ChatGPT [Large language model]. https://chat.openai.com
        ProblemData problemData = new ProblemData();
        JSONObject instance = (JSONObject) jsonObject.get("instance");
        JSONObject network = (JSONObject) instance.get("network");
        JSONArray nodesArray = (JSONArray) network.get("nodes");

        // read depot, customers, stations
        for (Object nodeObj : nodesArray){
            JSONObject nodeJSON = (JSONObject) nodeObj;
            int id = Integer.parseInt((String) nodeJSON.get("@id"));
            int type = Integer.parseInt((String) nodeJSON.get("@type"));
            double cx = Double.parseDouble((String) nodeJSON.get("cx"));
            double cy = Double.parseDouble((String) nodeJSON.get("cy"));
            if (type == 0) { // create depot
                problemData.depot = new Customer(id, (int)cx, (int)cy, true, 0);
            } else if (type == 1) { // create customer
                double demand = Double.parseDouble((String) nodeJSON.get("@demand")); // Extract demand from JSON if available
                problemData.customers.add(new Customer(id, (int)cx, (int)cy, false, demand));
            } else if (type == 2) { // Assuming type 2 is a station
                double chargingRate = Double.parseDouble((String) nodeJSON.get("charging_rate")); // Extract charging rate from JSON if available
                problemData.stations.add(new Station(id, (int)cx, (int)cy, chargingRate));
            }
        }

        // read vehicle profile
        JSONObject fleet = (JSONObject) instance.get("fleet");
        JSONObject vehicleProfile = (JSONObject) fleet.get("vehicle_profile");
        int truckCapacity = Integer.parseInt((String) vehicleProfile.get("capacity"));
        double truckMaxTravelDistance = (double) vehicleProfile.get("max_travel_distance");
        int truckCostPerKm = 10;
        problemData.truckParameters = new TruckParameters(truckCapacity, truckMaxTravelDistance, truckCostPerKm);

        problemData.reportProblemData();
        return problemData;
    }

}
