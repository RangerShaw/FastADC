package FastADC;

import FastADC.evidence.evidenceSet.Evidence;
import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraint;

import java.util.List;

public class CommonHelper {

    public static void checkDCs(List<LongBitSet> dcSet, Evidence[] evidences) {
        for (LongBitSet rawDC : dcSet) {
            long violation = 0L;
            for (Evidence evi : evidences) {
                if (rawDC.isSubSetOf(evi.getBitSetPredicates()))
                    violation += evi.getCount();
            }
            System.out.println(new DenialConstraint(rawDC) + "\t" + violation);
        }
    }

}
