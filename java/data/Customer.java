package data;

public record Customer(int id, int x, int y, boolean isDepot, double demand) implements INode {

    public double distanceTo(INode other) {
        return Math.sqrt(Math.pow(this.x - other.x(), 2) + Math.pow(this.y - other.y(), 2));
    }

    public String getCoordinates(){
        return "(" + this.x + "," + this.y + ")" ;
    }
}
