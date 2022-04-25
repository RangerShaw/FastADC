package FastADC.aei;

import ch.javasoft.bitset.LongBitSet;

public class DCCandidate {

    LongBitSet bitSet;
    LongBitSet cand;

    public DCCandidate(LongBitSet bitSet) {
        this.bitSet = bitSet;
    }

    public DCCandidate(LongBitSet bitSet, LongBitSet cand) {
        this.bitSet = bitSet;
        this.cand = cand;
    }

    @Override
    public int hashCode() {
        return bitSet.hashCode();
    }

    @Override
    public DCCandidate clone() {
        return new DCCandidate(bitSet.clone(), cand.clone());
    }
}
