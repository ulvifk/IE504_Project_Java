package data;

public class Triple<T, K, J> {
    public final T first;
    public final K second;
    public final J third;

    public Triple(T first, K second, J third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}
