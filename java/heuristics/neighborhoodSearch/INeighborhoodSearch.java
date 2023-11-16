package heuristics.neighborhoodSearch;

import data.ProblemData;
import heuristics.Neighbor;
import heuristics.Solution;

import java.util.List;

public interface INeighborhoodSearch {
    Solution solution();
    List<Neighbor> neighbors();
    ProblemData problemdata();
}
