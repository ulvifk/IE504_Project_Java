package heuristics.neighborhoodSearch.moves;

public interface IMove {
    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();
}
