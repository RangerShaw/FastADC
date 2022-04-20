package FastADC.evidence.clue;

import com.koloboke.collect.map.hash.HashLongLongMap;
import FastADC.plishard.Pli;
import FastADC.plishard.PliShard;

import java.util.List;

/**
 * To build the clue set of two Pli shards
 */
public class CrossClueSetBuilder extends ClueSetBuilder {

    private final List<Pli> plis1, plis2;
    private final int evidenceCount;

    public CrossClueSetBuilder(PliShard shard1, PliShard shard2) {
        plis1 = shard1.plis;
        plis2 = shard2.plis;
        evidenceCount = (shard1.end - shard1.beg) * (shard2.end - shard2.beg);
    }

    public HashLongLongMap buildClueSet() {
        long[] forwardClues = new long[evidenceCount];   // plis1 -> plis2
        long[] reverseClues = new long[evidenceCount];   // plis2 -> plis1

        for (PredicatePack catPack : strSinglePacks) {
            long eqMask = catPack.eqMask;
            int c1 = catPack.col1Index, c2 = catPack.col2Index;
            correctStrSingle(forwardClues, reverseClues, plis1.get(c1), plis2.get(c2), eqMask);
        }

        for (PredicatePack catPack : strCrossPacks) {
            long eqMask = catPack.eqMask;
            int c1 = catPack.col1Index, c2 = catPack.col2Index;
            correctStrCross(forwardClues, plis1.get(c1), plis2.get(c2), eqMask);
            correctStrCross(reverseClues, plis2.get(c1), plis1.get(c2), eqMask);
        }

        for (PredicatePack numPack : numSinglePacks) {
            long eqMask = numPack.eqMask, gtMask = numPack.gtMask;
            int c1 = numPack.col1Index, c2 = numPack.col2Index;
            correctNumSingle(forwardClues, reverseClues, plis1.get(c1), plis2.get(c2), eqMask, gtMask);
        }

        for (PredicatePack numPack : numCrossPacks) {
            long eqMask = numPack.eqMask, gtMask = numPack.gtMask;
            int c1 = numPack.col1Index, c2 = numPack.col2Index;
            correctNumCross(forwardClues, plis1.get(c1), plis2.get(c2), eqMask, gtMask);
            correctNumCross(reverseClues, plis2.get(c1), plis1.get(c2), eqMask, gtMask);
        }

        return accumulateClues(forwardClues, reverseClues);
    }

    private void setSingleEQ(long[] clues1, long[] clues2, Pli pli1, int i, Pli pli2, int j, long mask) {
        int beg1 = pli1.pliShard.beg, range1 = pli1.pliShard.end - beg1;
        int beg2 = pli2.pliShard.beg, range2 = pli2.pliShard.end - beg2;

        for (int tid1 : pli1.get(i).getRawCluster()) {
            int t1 = tid1 - beg1, r1 = t1 * range2 - beg2;
            for (int tid2 : pli2.get(j).getRawCluster()) {
                clues1[r1 + tid2] |= mask;
                clues2[(tid2 - beg2) * range1 + t1] |= mask;
            }
        }
    }

    private void correctStrSingle(long[] clues1, long[] clues2, Pli pivotPli, Pli probePli, long mask) {
        final int[] pivotKeys = pivotPli.getKeys();

        for (int i = 0; i < pivotKeys.length; i++) {
            Integer j = probePli.getClusterIdByKey(pivotKeys[i]);
            if (j != null)
                setSingleEQ(clues1, clues2, pivotPli, i, probePli, j, mask);
        }
    }

    private void setCrossEQ(long[] clues, Pli pli1, int i, Pli pli2, int j, long mask) {
        int tidBeg1 = pli1.pliShard.beg;
        int tidBeg2 = pli2.pliShard.beg, tidRange2 = pli2.pliShard.end - tidBeg2;

        for (int tid1 : pli1.get(i).getRawCluster()) {
            int r1 = (tid1 - tidBeg1) * tidRange2 - tidBeg2;
            for (int tid2 : pli2.get(j).getRawCluster()) {
                clues[r1 + tid2] |= mask;
            }
        }
    }

    private void correctStrCross(long[] clues, Pli pivotPli, Pli probePli, long mask) {
        final int[] pivotKeys = pivotPli.getKeys();

        for (int i = 0; i < pivotKeys.length; i++) {
            Integer j = probePli.getClusterIdByKey(pivotKeys[i]);
            if (j != null)
                setCrossEQ(clues, pivotPli, i, probePli, j, mask);
        }
    }

    // pli2 probe[0,to) -> pli1 pivot[i]
    private void setReverseGT(long[] reverseArray, Pli probePli, int to, Pli pivotPli, int i, long mask) {
        int probeBeg = probePli.pliShard.beg;
        int pivotBeg = pivotPli.pliShard.beg, pivotRange = pivotPli.pliShard.end - pivotBeg;

        for (int j = 0; j < to; j++) {
            for (int probeTid : probePli.get(j).getRawCluster()) {
                int r2 = (probeTid - probeBeg) * pivotRange - pivotBeg;
                for (int pivotTid : pivotPli.get(i).getRawCluster()) {
                    reverseArray[r2 + pivotTid] |= mask;
                }
            }
        }
    }

    // pli1 pivot[i] -> pli2 probe[from, probe.size)
    private void setForwardGT(long[] forwardArray, Pli pivotPli, int i, Pli probePli, int from, long mask) {
        int pivotBeg = pivotPli.pliShard.beg;
        int probeBeg = probePli.pliShard.beg, probeRange = probePli.pliShard.end - probeBeg;

        for (int pivotTid : pivotPli.get(i).getRawCluster()) {
            int r1 = (pivotTid - pivotBeg) * probeRange - probeBeg;
            for (int j = from; j < probePli.size(); j++) {
                for (int probeTid : probePli.get(j).getRawCluster()) {
                    forwardArray[r1 + probeTid] |= mask;
                }
            }
        }
    }

    private void correctNumSingle(long[] forwardArray, long[] reverseArray, Pli pivotPli, Pli probePli, long eqMask, long gtMask) {
        final int[] pivotKeys = pivotPli.getKeys();
        final int[] probeKeys = probePli.getKeys();

        for (int i = 0, j = 0; i < pivotKeys.length; i++) {
            int pivotKey = pivotKeys[i];
            j = probePli.getFirstIndexWhereKeyIsLTE(pivotKey, j);   // current position in probePli
            int reverseTo = j;

            if (j == probeKeys.length) {    // pivotKey is less than all probeKeys
                for (int ii = i; ii < pivotKeys.length; ii++)
                    setReverseGT(reverseArray, probePli, j, pivotPli, ii, gtMask);
                break;
            } else if (pivotKey == probeKeys[j]) {
                setSingleEQ(forwardArray, reverseArray, pivotPli, i, probePli, j, eqMask);
                j++;
            }

            setForwardGT(forwardArray, pivotPli, i, probePli, j, gtMask);
            setReverseGT(reverseArray, probePli, reverseTo, pivotPli, i, gtMask);
        }
    }

    private void correctNumCross(long[] forwardArray, Pli pivotPli, Pli probePli, long eqMask, long gtMask) {
        final int[] pivotKeys = pivotPli.getKeys();
        final int[] probekeys = probePli.getKeys();

        for (int i = 0, j = 0; i < pivotKeys.length; i++) {
            int key = pivotKeys[i];
            j = probePli.getFirstIndexWhereKeyIsLTE(key, j);    // current position in probePli

            if (j == probekeys.length)
                break;
            else if (key == probekeys[j]) {
                setCrossEQ(forwardArray, pivotPli, i, probePli, j, eqMask);
                j++;
            }

            setForwardGT(forwardArray, pivotPli, i, probePli, j, gtMask);
        }
    }

}
