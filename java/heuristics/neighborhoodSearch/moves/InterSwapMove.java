package heuristics.neighborhoodSearch.moves;

import data.INode;
import data.Truck;

public record InterSwapMove(Truck truck1, Truck truck2, INode node1, INode node2) implements IMove{
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof InterSwapMove)) {
            return false;
        }
        var other = (InterSwapMove) obj;
        return this.truck1.equals(other.truck1) &&
                this.truck2.equals(other.truck2) &&
                this.node1.equals(other.node1) &&
                this.node2.equals(other.node2);
    }
}
