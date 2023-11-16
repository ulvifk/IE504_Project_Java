package heuristics;
import heuristics.neighborhoodSearch.moves.IMove;

public record Neighbor(Solution solution, IMove move) implements Comparable<Neighbor>{
    @Override
    public int compareTo(Neighbor other) {
        return solution.compareTo(other.solution);
    }
}
