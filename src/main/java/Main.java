import FastADC.FastADC;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraintSet;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public class Main {

    static boolean singleColumn = false;

    static int shardLength = 350;

    static double threshold = 0.01d;

    static private String fp;

    static int rowLimit = -1;

    static int parallelism = -1;

    /*
    0: regular;
    1: read evidences from file;
    2: write evidences to file;
    3: build and write evidences only\n4: build evidences only
    */
    static int mode = 0;

    static boolean linear = false;

    public static void main(String[] args) {
        System.out.println("[ARGS]" + Arrays.stream(args).map(s -> s + ' ').collect(Collectors.joining()));
        test(-1);
    }

    static void test(int dataset) {
        if (parallelism > 0)
            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", Integer.toString(parallelism));
        System.out.println("PARALLELISM: " + (linear ? "single thread" : ForkJoinPool.commonPool().getParallelism()));

        FastADC fastADC = new FastADC(singleColumn, threshold, shardLength, mode, linear);
        DenialConstraintSet dcs = fastADC.buildApproxDCs(fp == null ? DataFp.DATA_FP[dataset] : fp, rowLimit);
        System.out.println();
    }

}
