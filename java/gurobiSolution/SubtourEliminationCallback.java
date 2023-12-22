import java.util.List;

import gurobi.*;

public class SubtourEliminationCallback extends GRBCallback {
    // OpenAI. "ChatGPT-4 Model." OpenAI, 2023, https://openai.com.
    private GRBVar[][][] x;
    private int numNodes;

    public SubtourEliminationCallback(GRBVar[][][] x, int numNodes) {
        this.x = x;
        this.numNodes = numNodes;
    }

    @Override
    protected void callback() {
        try {
            if (where == GRB.CB_MIPSOL) {
                // Find the solution
                double[][][] sol = getSolution(x);
                for (int v = 0; v < x.length; v++) {
                    // Check for subtours and add constraints if necessary
                    List<List<Integer>> subtours = findSubtours(sol[v]);
                    for (List<Integer> subtour : subtours) {
                        if (subtour.size() < numNodes) {
                            GRBLinExpr lhs = new GRBLinExpr();
                            for (int i = 0; i < subtour.size(); i++) {
                                for (int j = i + 1; j < subtour.size(); j++) {
                                    int nodeI = subtour.get(i);
                                    int nodeJ = subtour.get(j);
                                    lhs.addTerm(1.0, x[v][nodeI][nodeJ]);
                                    lhs.addTerm(1.0, x[v][nodeJ][nodeI]);
                                }
                            }
                            addLazy(lhs, GRB.LESS_EQUAL, subtour.size() - 1);
                        }
                    }
                }
            }
        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
            e.printStackTrace();
        }
    }
}

// Register the callback with the model
model.setCallback(new SubtourEliminationCallback(x, numNodes));
