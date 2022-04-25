import FastADC.FastADC;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraintSet;

public class Main {

    public static void main(String[] args) {
        String fp = "./dataset/atom.csv";
        int rowLimit = -1;              // limit the number of tuples in dataset, -1 means no limit
        boolean singleColumn = false;   // only single-attribute predicates
        int shardLength = 350;
        double threshold = 0.01d;
        boolean linear = false;         // linear single-thread in EvidenceSetBuilder

        FastADC fastADC = new FastADC(singleColumn, threshold, shardLength, linear);
        DenialConstraintSet dcs = fastADC.buildApproxDCs(fp, rowLimit);
        System.out.println();
    }


}
