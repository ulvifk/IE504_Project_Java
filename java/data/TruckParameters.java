package data;

public class TruckParameters {
    public int truckCapacity;
    public double truckMaxTravelDistance;
    public int truckCostPerKm;

    public TruckParameters(int truckCapacity, double truckMaxTravelDistance, int truckCostPerKm) {
        this.truckCapacity = truckCapacity;
        this.truckMaxTravelDistance = truckMaxTravelDistance;
        this.truckCostPerKm = truckCostPerKm;
    }

    public String toString(){
        return "capacity: "+ this.truckCapacity + ", max distance: " + this.truckMaxTravelDistance + ", unit cost:" + this.truckCostPerKm ;
    }
}
