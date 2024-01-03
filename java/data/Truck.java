package data;

public record Truck (int id, double capacity, double batteryCapacity, double costPerKm) {

    @Override
    public int hashCode() {
        // Use memory address as hashcode
        return System.identityHashCode(this);
    }

    public double batteryConsumption(double dist){
        return dist * costPerKm;
    }
}
