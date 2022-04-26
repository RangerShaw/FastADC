package FastADC.evidence.clue;

import ch.javasoft.bitset.LongBitSet;
import com.koloboke.collect.map.hash.HashLongLongMap;
import com.koloboke.collect.map.hash.HashLongLongMaps;
import de.metanome.algorithms.dcfinder.predicates.Operator;
import de.metanome.algorithms.dcfinder.predicates.Predicate;
import FastADC.predicate.PredicateBuilder;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;

import java.util.*;

abstract public class ClueSetBuilder {

    abstract public HashLongLongMap buildClueSet();

    HashLongLongMap accumulateClues(long[]... clueArrays) {
        HashLongLongMap clueSet = HashLongLongMaps.newMutableMap();
        for (long[] map : clueArrays)
            for (long clue : map)
                clueSet.addValue(clue, 1L, 0L);
        return clueSet;
    }

    /**
     * Predicate EQ (and GT) of one column pair
     */
    static class PredicatePack {
        Predicate eq, gt;
        int col1Index, col2Index;
        long eqMask, gtMask;

        public PredicatePack(Predicate eq, int eqPos) {
            this.eq = eq;
            col1Index = eq.getOperand1().getColumn().getIndex();
            col2Index = eq.getOperand2().getColumn().getIndex();
            eqMask = 1L << eqPos;
        }

        public PredicatePack(Predicate eq, int eqPos, Predicate gt, int gtPos) {
            this.eq = eq;
            this.gt = gt;
            col1Index = eq.getOperand1().getColumn().getIndex();
            col2Index = eq.getOperand2().getColumn().getIndex();
            eqMask = 1L << eqPos;
            gtMask = 1L << gtPos;
        }
    }

    static LongBitSet[] correctionMap;    // Predicate id -> its correction mask

    static List<PredicatePack> strSinglePacks;  // String single-column predicate packs
    static List<PredicatePack> strCrossPacks;   // String cross-column predicate packs
    static List<PredicatePack> numSinglePacks;  // numerical single-column predicate packs
    static List<PredicatePack> numCrossPacks;   // numerical cross-column predicate packs

    public static void configure(PredicateBuilder pBuilder) {
        strSinglePacks = new ArrayList<>();
        strCrossPacks = new ArrayList<>();
        numSinglePacks = new ArrayList<>();
        numCrossPacks = new ArrayList<>();

        buildPredicateGroupsAndCorrectMap(pBuilder);
    }

    static public LongBitSet[] getCorrectionMap() {
        return correctionMap;
    }

    private static void buildPredicateGroupsAndCorrectMap(PredicateBuilder pBuilder) {
        List<List<Predicate>> catSingle = pBuilder.getPredicateGroupsCategoricalSingleColumn();
        List<List<Predicate>> catCross = pBuilder.getStrCrossColumnPredicateGroups();
        List<List<Predicate>> numSingle = pBuilder.getPredicateGroupsNumericalSingleColumn();
        List<List<Predicate>> numCross = pBuilder.getPredicateGroupsNumericalCrossColumn();

        correctionMap = new LongBitSet[catSingle.size() + catCross.size() + 2 * numSingle.size() + 2 * numCross.size()];

        int count = 0;

        for (Collection<Predicate> predicateGroup : catSingle) {
            Predicate eq = pBuilder.getPredicateByType(predicateGroup, Operator.EQUAL);
            correctionMap[count] = buildCatEQCorrectMasks(predicateGroup, pBuilder);
            strSinglePacks.add(new PredicatePack(eq, count++));
        }

        for (Collection<Predicate> predicateGroup : catCross) {
            Predicate eq = pBuilder.getPredicateByType(predicateGroup, Operator.EQUAL);
            correctionMap[count] = buildCatEQCorrectMasks(predicateGroup, pBuilder);
            strCrossPacks.add(new PredicatePack(eq, count++));
        }

        for (Collection<Predicate> predicateGroup : numSingle) {
            Predicate eq = pBuilder.getPredicateByType(predicateGroup, Operator.EQUAL);
            Predicate gt = pBuilder.getPredicateByType(predicateGroup, Operator.GREATER);
            correctionMap[count] = buildNumEQCorrectMasks(predicateGroup, pBuilder);
            correctionMap[count + 1] = buildNumGTCorrectMasks(predicateGroup, pBuilder);
            numSinglePacks.add(new PredicatePack(eq, count, gt, count + 1));
            count += 2;
        }

        for (Collection<Predicate> predicateGroup : numCross) {
            Predicate eq = pBuilder.getPredicateByType(predicateGroup, Operator.EQUAL);
            Predicate gt = pBuilder.getPredicateByType(predicateGroup, Operator.GREATER);
            correctionMap[count] = buildNumEQCorrectMasks(predicateGroup, pBuilder);
            correctionMap[count + 1] = buildNumGTCorrectMasks(predicateGroup, pBuilder);
            numCrossPacks.add(new PredicatePack(eq, count, gt, count + 1));
            count += 2;
        }

        System.out.println("  [CLUE] # of bits in clue: " + count);
        if (count > 64) throw new UnsupportedOperationException("Too many predicates! Not supported yet!");
    }

    private static LongBitSet buildCatEQCorrectMasks(Collection<Predicate> predicateGroup, PredicateBuilder pBuilder) {
        PredicateSet eqMask = new PredicateSet();

        Predicate eq = pBuilder.getPredicateByType(predicateGroup, Operator.EQUAL);
        Predicate neq = pBuilder.getPredicateByType(predicateGroup, Operator.UNEQUAL);

        eqMask.add(eq);
        eqMask.add(neq);

        return eqMask.getBitset();
    }

    private static LongBitSet buildNumEQCorrectMasks(Collection<Predicate> predicateGroup, PredicateBuilder pBuilder) {
        PredicateSet eqMask = new PredicateSet();

        Predicate eq = pBuilder.getPredicateByType(predicateGroup, Operator.EQUAL);
        Predicate neq = pBuilder.getPredicateByType(predicateGroup, Operator.UNEQUAL);
        Predicate lt = pBuilder.getPredicateByType(predicateGroup, Operator.LESS);
        Predicate gte = pBuilder.getPredicateByType(predicateGroup, Operator.GREATER_EQUAL);

        eqMask.add(eq);
        eqMask.add(neq);
        eqMask.add(lt);
        eqMask.add(gte);

        return eqMask.getBitset();
    }

    private static LongBitSet buildNumGTCorrectMasks(Collection<Predicate> predicateGroup, PredicateBuilder pBuilder) {
        PredicateSet gtMask = new PredicateSet();

        Predicate lt = pBuilder.getPredicateByType(predicateGroup, Operator.LESS);
        Predicate lte = pBuilder.getPredicateByType(predicateGroup, Operator.LESS_EQUAL);
        Predicate gt = pBuilder.getPredicateByType(predicateGroup, Operator.GREATER);
        Predicate gte = pBuilder.getPredicateByType(predicateGroup, Operator.GREATER_EQUAL);

        gtMask.add(lt);
        gtMask.add(lte);
        gtMask.add(gt);
        gtMask.add(gte);

        return gtMask.getBitset();
    }

}

