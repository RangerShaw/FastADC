package FastADC.evidence.evidenceSet;

import ch.javasoft.bitset.LongBitSet;
import com.koloboke.collect.map.hash.*;
import de.metanome.algorithms.dcfinder.predicates.Operator;
import de.metanome.algorithms.dcfinder.predicates.Predicate;
import FastADC.predicate.PredicateBuilder;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;

import java.util.*;
import java.util.stream.Collectors;

/**
 * record evidences and their counts
 */
public class EvidenceSet implements Iterable<Evidence> {

    private LongBitSet cardinalityMask;
    private LongBitSet[] correctMap;

    private HashMap<Long, Evidence> clueToEvidence = new HashMap<>();


    public EvidenceSet(PredicateBuilder pBuilder, LongBitSet[] _correctMap) {
        correctMap = _correctMap;
        cardinalityMask = buildCardinalityMask(pBuilder);
    }

    public void build(HashLongLongMap clueSet) {
        for (var entry : clueSet.entrySet()) {
            long clue = entry.getKey();
            Evidence evi = new Evidence(clue, entry.getValue(), cardinalityMask, correctMap);
            clueToEvidence.put(clue, evi);
            //evidenceSet.addValue(evi, count, 0L);
        }
    }

    public int size() {
        return clueToEvidence.size();
    }

    public long getTotalCount() {
        return clueToEvidence.values().stream().mapToLong(e -> e.count).reduce(0L, Long::sum);
    }

    public Collection<Evidence> getEvidences() {
        return clueToEvidence.values();
    }

    public Collection<LongBitSet> getRawEvidences() {
        return clueToEvidence.values().stream().map(e->e.bitset).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        clueToEvidence.forEach((k, v) -> sb.append(k.toString() + "\t" + v + "\n"));
        return sb.toString();
    }


    private LongBitSet buildCardinalityMask(PredicateBuilder predicateBuilder) {
        PredicateSet cardinalityPredicateBitset = new PredicateSet();

        for (Collection<Predicate> predicateGroup : predicateBuilder.getPredicateGroupsCategoricalSingleColumn()) {
            Predicate neq = predicateBuilder.getPredicateByType(predicateGroup, Operator.UNEQUAL);
            cardinalityPredicateBitset.add(neq);
        }

        for (Collection<Predicate> predicateGroup : predicateBuilder.getStrCrossColumnPredicateGroups()) {
            Predicate neq = predicateBuilder.getPredicateByType(predicateGroup, Operator.UNEQUAL);
            cardinalityPredicateBitset.add(neq);
        }

        for (Collection<Predicate> predicateGroup : predicateBuilder.getPredicateGroupsNumericalSingleColumn()) {
            Predicate neq = predicateBuilder.getPredicateByType(predicateGroup, Operator.UNEQUAL);
            Predicate lt = predicateBuilder.getPredicateByType(predicateGroup, Operator.LESS);
            Predicate lte = predicateBuilder.getPredicateByType(predicateGroup, Operator.LESS_EQUAL);
            cardinalityPredicateBitset.add(neq);
            cardinalityPredicateBitset.add(lt);
            cardinalityPredicateBitset.add(lte);
        }

        for (Collection<Predicate> predicateGroup : predicateBuilder.getPredicateGroupsNumericalCrossColumn()) {
            Predicate neq = predicateBuilder.getPredicateByType(predicateGroup, Operator.UNEQUAL);
            Predicate lt = predicateBuilder.getPredicateByType(predicateGroup, Operator.LESS);
            Predicate lte = predicateBuilder.getPredicateByType(predicateGroup, Operator.LESS_EQUAL);
            cardinalityPredicateBitset.add(neq);
            cardinalityPredicateBitset.add(lt);
            cardinalityPredicateBitset.add(lte);
        }

        return cardinalityPredicateBitset.getBitset();
    }

    @Override
    public Iterator<Evidence> iterator() {
        return clueToEvidence.values().iterator();
    }

    public Evidence[] toArray() {
        return clueToEvidence.values().toArray(new Evidence[0]);
    }
}
