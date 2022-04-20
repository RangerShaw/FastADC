package FastADC.evidence.clue;

import com.koloboke.collect.map.hash.HashLongLongMap;
import FastADC.plishard.Cluster;
import FastADC.plishard.Pli;
import FastADC.plishard.PliShard;

import java.util.List;

/**
 * To build the clue set of one Pli shard
 */
public class SingleClueSetBuilder extends ClueSetBuilder {

    private final List<Pli> plis;
    private final int tidBeg, tidRange;
    private final int evidenceCount;

    public SingleClueSetBuilder(PliShard shard) {
        plis = shard.plis;
        tidBeg = shard.beg;
        tidRange = shard.end - shard.beg;
        evidenceCount = tidRange * tidRange;
    }

    public HashLongLongMap buildClueSet() {
        long[] clues = new long[evidenceCount];

        for (PredicatePack catPack : strSinglePacks) {
            int c = catPack.col1Index;
            correctStrSingle(clues, plis.get(c), catPack.eqMask);
        }

        for (PredicatePack catPack : strCrossPacks) {
            int c1 = catPack.col1Index, c2 = catPack.col2Index;
            correctStrCross(clues, plis.get(c1), plis.get(c2), catPack.eqMask);
        }

        for (PredicatePack numPack : numSinglePacks) {
            int c = numPack.col1Index;
            correctNumSingle(clues, plis.get(c), numPack.eqMask, numPack.gtMask);
        }

        for (PredicatePack numPack : numCrossPacks) {
            int c1 = numPack.col1Index, c2 = numPack.col2Index;
            correctNumCross(clues, plis.get(c1), plis.get(c2), numPack.eqMask, numPack.gtMask);
        }

        HashLongLongMap clueSet = accumulateClues(clues);

        if (0L == clueSet.addValue(0L, -tidRange))     // remove reflex evidence
            clueSet.remove(0L);

        return clueSet;
    }

    private void setSingleEQ(long[] clues, Cluster cluster, long mask) {
        List<Integer> rawCluster = cluster.getRawCluster();

        for (int i = 0; i < rawCluster.size() - 1; i++) {
            int t1 = rawCluster.get(i) - tidBeg, r1 = t1 * tidRange;
            for (int j = i + 1; j < rawCluster.size(); j++) {
                int tid2 = rawCluster.get(j) - tidBeg;
                clues[r1 + tid2] |= mask;               // (cluster.get(i)-tidBeg)*tidRange + (cluster.get(j)-tidBeg)
                clues[tid2 * tidRange + t1] |= mask;    // (cluster.get(j)-tidBeg)*tidRange + (cluster.get(i)-tidBeg)
            }
        }
    }

    private void correctStrSingle(long[] clues, Pli pli, long mask) {
        for (int i = 0; i < pli.size(); i++) {
            if (pli.get(i).size() > 1)
                setSingleEQ(clues, pli.get(i), mask);
        }
    }

    private void setCrossEQ(long[] clues, Cluster pivotCluster, Cluster probeCluster, long mask) {
        for (int tid1 : pivotCluster.getRawCluster()) {
            int r1 = (tid1 - tidBeg) * tidRange - tidBeg;
            for (int tid2 : probeCluster.getRawCluster())
                if (tid1 != tid2)
                    clues[r1 + tid2] |= mask;
        }
    }

    private void correctStrCross(long[] clues, Pli pivotPli, Pli probePli, long mask) {
        final int[] pivotKeys = pivotPli.getKeys();

        for (int i = 0; i < pivotKeys.length; i++) {
            Integer j = probePli.getClusterIdByKey(pivotKeys[i]);
            if (j != null)
                setCrossEQ(clues, pivotPli.get(i), probePli.get(j), mask);
        }
    }

    private void setGT(long[] clues, Cluster pivotCluster, Pli probePli, int from, long mask) {
        for (int pivotTid : pivotCluster.getRawCluster()) {
            int r1 = (pivotTid - tidBeg) * tidRange - tidBeg;
            for (int j = from; j < probePli.size(); j++) {
                for (int probeTid : probePli.get(j).getRawCluster()) {
                    if (pivotTid != probeTid)
                        clues[r1 + probeTid] |= mask;
                }
            }
        }
    }

    private void correctNumSingle(long[] clues, Pli pli, long eqMask, long gtMask) {
        for (int i = 0; i < pli.size(); i++) {
            Cluster cluster = pli.get(i);
            if (cluster.size() > 1)
                setSingleEQ(clues, cluster, eqMask);
            if (i < pli.size() - 1)
                setGT(clues, cluster, pli, i + 1, gtMask);
        }
    }

    private void correctNumCross(long[] clues, Pli pivotPli, Pli probePli, long eqMask, long gtMask) {
        final int[] pivotKeys = pivotPli.getKeys();
        final int[] probeKeys = probePli.getKeys();

        for (int i = 0, j = 0; i < pivotKeys.length; i++) {
            int key = pivotKeys[i];
            j = probePli.getFirstIndexWhereKeyIsLTE(key, j);    // current position in probePli

            if (j == probeKeys.length)
                break;
            else if (key == probeKeys[j]) {
                setCrossEQ(clues, pivotPli.get(i), probePli.get(j), eqMask);
                j++;
            }

            setGT(clues, pivotPli.get(i), probePli, j, gtMask);
        }
    }

}
