package data;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ProblemData {

    public Customer depot;
    public List<Customer> customers;
    public List<Station> stations;
    public TruckParameters truckParameters;

    public ProblemData() {
        this.customers = new LinkedList<>();
        this.stations = new LinkedList<>();
        this.truckParameters = new TruckParameters(0, 0, 0);
    }

    public void reportProblemData() {
        System.out.println("Depot coordinates: "+ depot.getCoordinates());
        System.out.println(String.format("%d many customers", customers.size()));
        System.out.println(String.format("%d many stations", stations.size()));
        System.out.println(truckParameters.toString());
    }
}
