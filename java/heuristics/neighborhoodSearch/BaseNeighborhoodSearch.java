package heuristics.neighborhoodSearch;

import heuristics.Neighbor;
import heuristics.Solution;

import java.util.LinkedList;
import java.util.List;

public abstract class BaseNeighborhoodSearch {
    public List<Neighbor> neighbors;
    protected Solution solution;

    public BaseNeighborhoodSearch(Solution solution) {
        this.solution = solution;
        this.neighbors = new LinkedList<>();
    }

    abstract void SearchNeighbors();

    abstract Solution applyMove();
}
