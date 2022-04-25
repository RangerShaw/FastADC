package FastADC.aei;

import ch.javasoft.bitset.LongBitSet;

import java.util.Collection;

public class SearchNode {

    int e;
    LongBitSet addablePredicates;
    LongBitSetTrie dcCandidates;
    Collection<DCCandidate> invalidDCs;
    long target;

    String H;

    public SearchNode(int e, LongBitSet addablePredicates, LongBitSetTrie dcCandidates, Collection<DCCandidate> invalidDCs, long target) {
        this.e = e;
        this.addablePredicates = addablePredicates;
        this.dcCandidates = dcCandidates;
        this.invalidDCs = invalidDCs;
        this.target = target;
    }

    public SearchNode(int e, LongBitSet addablePredicates, LongBitSetTrie dcCandidates,
                      Collection<DCCandidate> invalidDCs, long target, String status) {
        this.e = e;
        this.addablePredicates = addablePredicates;
        this.dcCandidates = dcCandidates;
        this.invalidDCs = invalidDCs;
        this.target = target;
        H = status;
    }
}
