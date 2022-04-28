import FastADC.FastADC;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraintSet;

public class Main {

    public static void main(String[] args) {
        String fp = "./dataset/airport.csv";
        double threshold = 0.01d;
        int rowLimit = -1;              // limit the number of tuples in dataset, -1 means no limit
        int shardLength = 350;
        boolean linear = false;         // linear single-thread in EvidenceSetBuilder
        boolean singleColumn = false;   // only single-attribute predicates

        FastADC fastADC = new FastADC(singleColumn, threshold, shardLength, linear);
        DenialConstraintSet dcs = fastADC.buildApproxDCs(fp, rowLimit);
        System.out.println();
    }

}
