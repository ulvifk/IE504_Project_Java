package data;

public record Node(int id, boolean isDepot, int x, int y, double demand) implements INode {

    public double distanceTo(INode other) {
        return Math.sqrt(Math.pow(this.x - other.x(), 2) + Math.pow(this.y - other.y(), 2));
    }
}
