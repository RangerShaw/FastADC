package FastADC.plishard;

import java.util.ArrayList;
import java.util.List;

public class Cluster {

    List<Integer> tuples;

    public Cluster() {
        tuples = new ArrayList<>();
    }

    public int get(int i) {
        return tuples.get(i);
    }

    public int size() {
        return tuples.size();
    }

    public List<Integer> getRawCluster() {
        return tuples;
    }

    public boolean isEmpty() {
        return tuples.isEmpty();
    }

    public void add(int row) {
        tuples.add(row);
    }

    public void add(Cluster c) {
        tuples.addAll(c.tuples);
    }

    @Override
    public String toString() {
        return tuples.toString();
    }
}
