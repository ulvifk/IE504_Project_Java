package data;

public record Truck (int id, double capacity, double batteryCapacity, double costPerKm) {

    public double batteryConsumption(double dist){
        return dist * costPerKm;
    }
}
