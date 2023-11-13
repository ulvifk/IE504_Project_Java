package data;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ProblemData {

    public List<Node> customers;
    public List<Station> stations;
    public Node depot;
    public ProblemParameters problemParameters;

    public ProblemData() {
        this.customers = new LinkedList<>();
        this.stations = new LinkedList<>();
    }
}
