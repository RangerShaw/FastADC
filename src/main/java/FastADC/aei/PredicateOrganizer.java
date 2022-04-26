package FastADC.aei;

import FastADC.evidence.evidenceSet.Evidence;
import FastADC.evidence.evidenceSet.EvidenceSet;
import ch.javasoft.bitset.LongBitSet;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

public class PredicateOrganizer {

    private final int nPredicates;
    private final EvidenceSet evidenceSet;
    private final int[] indexes;    // new index -> original index

    public PredicateOrganizer(int n, EvidenceSet evidences) {
        nPredicates = n;
        evidenceSet = evidences;

        int[] coverages = getPredicateCoverage(evidenceSet);
        indexes = createIndexArray(coverages);
    }

    private int[] getPredicateCoverage(EvidenceSet evidenceSet) {
        int[] counts = new int[nPredicates];
        for (Evidence evidence : evidenceSet) {
            LongBitSet bitset = evidence.getBitSetPredicates();
            for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
                counts[i]++;
            }
        }
        return counts;
    }

    public int[] createIndexArray(int[] coverages) {
        return IntStream.range(0, coverages.length)
                .boxed()
                .sorted(Comparator.comparingInt(i -> coverages[i]))
                .mapToInt(Integer::intValue)
                .toArray();
    }

    public Evidence[] transformEvidenceSet() {
        Evidence[] evidenceArray = new Evidence[evidenceSet.size()];
        int n = 0;
        for (Evidence e : evidenceSet) {
            evidenceArray[n] = new Evidence(transform(e.bitset), e.count);
            n++;
        }
        return evidenceArray;
    }

    public LongBitSet transform(LongBitSet bitset) {
        LongBitSet bitset2 = new LongBitSet();
        for (Integer i : indexes) {
            if (bitset.get(indexes[i]))
                bitset2.set(i);
        }
        return bitset2;
    }

    public LongBitSet[] transformMutexMap(LongBitSet[] mutexMap) {
        LongBitSet[] transMutexMap = new LongBitSet[mutexMap.length];
        for (int i = 0; i < mutexMap.length; i++) {
            transMutexMap[transform(i)] = transform(mutexMap[i]);
        }
        return transMutexMap;
    }

    public int transform(int e) {
        for (int i : indexes)
            if (e == indexes[i]) return i;
        return -1;
    }

    public LongBitSet retransform(LongBitSet bitset) {
        LongBitSet valid = new LongBitSet();
        for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
            valid.set(indexes[i]);
        }
        return valid;
    }

}
