package data;

public record Station(int id, int x, int y, double chargingRate) implements INode{

    public double distanceTo(INode other) {
        return Math.sqrt(Math.pow(this.x() - other.x(), 2) + Math.pow(this.y() - other.y(), 2));
    }

    public double getChargingRate(){
        return 0.2;
    }
}
