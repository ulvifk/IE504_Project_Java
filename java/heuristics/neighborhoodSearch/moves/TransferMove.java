package heuristics.neighborhoodSearch.moves;

import data.INode;
import data.Truck;

public record TransferMove(Truck fromTruck, Truck toTruck, INode node, int toIndex) implements IMove {

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TransferMove)) {
            return false;
        }
        var other = (TransferMove) obj;
        return this.fromTruck.equals(other.fromTruck) &&
                this.toTruck.equals(other.toTruck) &&
                this.node.equals(other.node);
    }
}
