package data;

public interface INode {
    int id();
    int x();
    int y();
    double distanceTo(INode other);
}
