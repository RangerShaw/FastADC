package FastADC.evidence.evidenceSet;

import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;

public class Evidence {

    public long count;
    private long clue;
    public LongBitSet bitset;

    public Evidence(long _satisfied, long _count, LongBitSet cardinalityMask, LongBitSet[] correctMap) {
        clue = _satisfied;
        count = _count;
        //predicates = buildEvidenceFromClue(cardinalityMask, correctMap);
        buildEvidenceFromClue(cardinalityMask, correctMap);
    }

    public Evidence(LongBitSet bitSet, long _count) {
        bitset = bitSet;
        count = _count;
    }

    public PredicateSet buildEvidenceFromClue(LongBitSet cardinalityMask, LongBitSet[] correctMap) {
        LongBitSet evidence = cardinalityMask.clone();

        long tmp = clue;
        int pos = 0;
        while (tmp != 0) {
            if ((tmp & 1L) != 0L) evidence.xor(correctMap[pos]);
            pos++;
            tmp >>>= 1;
        }

        bitset = evidence;
        return new PredicateSet(evidence);
    }

    public LongBitSet getBitSetPredicates() {
        return bitset;
    }

    public long getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evidence evidence = (Evidence) o;
        return clue == evidence.clue;
    }

    @Override
    public int hashCode() {
        return (int) (clue ^ (clue >>> 32));
    }

}
