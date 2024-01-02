package data;

public record TruckResourceCache(Truck truck, INode node, double batteryAtNode, double capacityAfterServingNode) implements Cloneable {

    public TruckResourceCache clone(){
        try {
            return (TruckResourceCache) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
