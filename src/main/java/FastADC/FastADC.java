package FastADC;

import FastADC.evidence.EvidenceSetBuilder;
import FastADC.evidence.evidenceSet.EvidenceSet;
import FastADC.aei.approx.ApproxEvidenceInverter;
import FastADC.predicate.PredicateBuilder;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraintSet;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.input.RelationalInput;
import FastADC.plishard.PliShard;
import FastADC.plishard.PliShardBuilder;

public class FastADC {

    // configures of PredicateBuilder
    private final boolean noCrossColumn;
    private final double minimumSharedValue = 0.3d;
    private final double comparableThreshold = 0.1d;

    // configure of PliShardBuilder
    private final int shardLength;

    // configure of ApproxCoverSearcher
    private final double threshold;

    private final int mode;
    private final boolean linear;

    private String dataFp;
    private Input input;
    private PredicateBuilder predicateBuilder;
    private PliShardBuilder pliShardBuilder;
    private EvidenceSetBuilder evidenceSetBuilder;

    public FastADC(boolean _noCrossColumn, double _threshold, int _len, int _mode, boolean _linear) {
        noCrossColumn = _noCrossColumn;
        threshold = _threshold;
        shardLength = _len;
        mode = _mode;
        linear = _linear;
        predicateBuilder = new PredicateBuilder(noCrossColumn, minimumSharedValue, comparableThreshold);
    }

    public DenialConstraintSet buildApproxDCs(String _dataFp, int sizeLimit) {
        dataFp = _dataFp;
        System.out.println("INPUT FILE: " + dataFp);
        System.out.println("ERROR THRESHOLD: " + threshold);

        // load input data, build predicate space
        long t00 = System.currentTimeMillis();
        input = new Input(new RelationalInput(dataFp), sizeLimit);
        predicateBuilder.buildPredicateSpace(input);
        System.out.println(" [ADCD] Predicate space size: " + predicateBuilder.predicateCount());

        // build pli shards
        pliShardBuilder = new PliShardBuilder(shardLength, input.getParsedColumns());
        PliShard[] pliShards = pliShardBuilder.buildPliShards(input.getIntInput());
        long t_pre = System.currentTimeMillis() - t00;
        System.out.println("[ADCD] Pre-process time: " + t_pre + "ms");

        // build evidence set
        long t10 = System.currentTimeMillis();
        evidenceSetBuilder = new EvidenceSetBuilder(predicateBuilder);
        EvidenceSet evidenceSet = evidenceSetBuilder.buildEvidenceSet(pliShards, linear);
        long t_evi = System.currentTimeMillis() - t10;
        System.out.println(" [ADCD] evidence set size: " + evidenceSet.size());
        System.out.println(" [ADCD] evidence count: " + evidenceSet.getTotalCount());
        System.out.println("[ADCD] Evidence time: " + t_evi + "ms");

        // approx evidence inversion
        long t20 = System.currentTimeMillis();
        long rowCount = input.getRowCount(), tuplePairCount = (rowCount - 1) * rowCount;
        long leastEvidenceToCover = (long) Math.ceil((1 - threshold) * tuplePairCount);
        System.out.println(" [ADCD] Violate at most " + (tuplePairCount - leastEvidenceToCover) + " tuple pairs");

        ApproxEvidenceInverter evidenceInverter = new ApproxEvidenceInverter(predicateBuilder, true);
        DenialConstraintSet dcs = evidenceInverter.buildDenialConstraints(evidenceSet, leastEvidenceToCover);
        long t_aei = System.currentTimeMillis() - t20;
        System.out.println("[ADCD] AEI time: " + t_aei + "ms");

        System.out.println("[ADCD] Total computing time: " + (t_pre + t_evi + t_aei) + " ms\n");
        return dcs;
    }

}
