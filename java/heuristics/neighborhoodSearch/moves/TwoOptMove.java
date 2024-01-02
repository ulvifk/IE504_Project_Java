package heuristics.neighborhoodSearch.moves;

import data.INode;
import data.Truck;

public record TwoOptMove(Truck truck, int i, int j) implements IMove {

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof TwoOptMove other)) return false;

        var isTruckEqual = truck.id() == other.truck().id();
        var isNodes1 = i == other.i();
        var isNodes2 = j == other.j();

        return isTruckEqual && isNodes1 && isNodes2;
    }
}
