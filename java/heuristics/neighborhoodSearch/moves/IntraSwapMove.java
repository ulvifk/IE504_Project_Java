package heuristics.neighborhoodSearch.moves;

import data.INode;
import data.Truck;

public record IntraSwapMove(Truck truck, INode node1, INode node2 ) implements IMove{

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof IntraSwapMove other)) return false;


        var isTruckEqual = truck.id() == other.truck().id();
        var isNodes1 = node1.id() == other.node1().id();
        var isNodes2 = node2.id() == other.node2().id();

        return isTruckEqual && isNodes1 && isNodes2;
    }
}
