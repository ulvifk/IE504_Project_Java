package heuristics.tabuSearch;


import heuristics.neighborhoodSearch.moves.IMove;

public class Tabu {
    public IMove move;
    public int remainingIteration;

    public Tabu(IMove move, int remainingIteration) {
        this.move = move;
        this.remainingIteration = remainingIteration;
    }
}
