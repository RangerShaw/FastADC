package de.metanome.algorithms.dcfinder.denialconstraints;

import java.util.*;
import java.util.Map.Entry;


import FastADC.predicate.PredicateBuilder;
import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;
import ch.javasoft.bitset.search.NTreeSearch;
import de.metanome.algorithms.dcfinder.predicates.sets.Closure;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSetFactory;

public class DenialConstraintSet implements Iterable<DenialConstraint> {

    private Set<DenialConstraint> constraints = new HashSet<>();

    public DenialConstraintSet() {
    }

    public DenialConstraintSet(PredicateBuilder builder, List<LongBitSet> covers) {
        for (LongBitSet s : covers)
            constraints.add(new DenialConstraint(builder.getInverse(s)));
    }

    public boolean contains(DenialConstraint dc) {
        return constraints.contains(dc);
    }

    public void add(DenialConstraint dc) {
        constraints.add(dc);
    }

    @Override
    public Iterator<DenialConstraint> iterator() {
        return constraints.iterator();
    }

    public int size() {
        return constraints.size();
    }


    private static class MinimalDCCandidate {
        DenialConstraint dc;
        IBitSet bitset;

        public MinimalDCCandidate(DenialConstraint dc) {
            this.dc = dc;
            this.bitset = PredicateSetFactory.create(dc.getPredicateSet()).getBitset();
        }

        public boolean shouldReplace(MinimalDCCandidate prior) {
            if (prior == null)
                return true;
            if (dc.getPredicateCount() < prior.dc.getPredicateCount())
                return true;
            if (dc.getPredicateCount() > prior.dc.getPredicateCount())
                return false;

            return bitset.compareTo(prior.bitset) <= 0;
        }
    }

    public void minimize() {
        Map<PredicateSet, MinimalDCCandidate> constraintsClosureMap = new HashMap<>();
        for (DenialConstraint dc : constraints) {
            PredicateSet predicateSet = dc.getPredicateSet();
            Closure c = new Closure(predicateSet);
            if (c.construct()) {
                MinimalDCCandidate candidate = new MinimalDCCandidate(dc);
                PredicateSet closure = c.getClosure();
                MinimalDCCandidate prior = constraintsClosureMap.get(closure);
                if (candidate.shouldReplace(prior))
                    constraintsClosureMap.put(closure, candidate);
            }
        }

        List<Entry<PredicateSet, MinimalDCCandidate>> constraints2 = new ArrayList<>(constraintsClosureMap.entrySet());

        constraints2.sort(Comparator
                .comparingInt((Entry<PredicateSet, MinimalDCCandidate> entry) -> entry.getKey().size())
                .thenComparingInt(entry -> entry.getValue().dc.getPredicateCount())
                .thenComparing(entry -> entry.getValue().bitset));

        constraints = new HashSet<>();
        NTreeSearch tree = new NTreeSearch();
        for (Entry<PredicateSet, MinimalDCCandidate> entry : constraints2) {
            if (tree.containsSubset(PredicateSetFactory.create(entry.getKey()).getBitset()))
                continue;

            DenialConstraint inv = entry.getValue().dc.getInvT1T2DC();
            if (inv != null) {
                Closure c = new Closure(inv.getPredicateSet());
                if (!c.construct())
                    continue;
                if (tree.containsSubset(PredicateSetFactory.create(c.getClosure()).getBitset()))
                    continue;
            }

            constraints.add(entry.getValue().dc);
            tree.add((LongBitSet) entry.getValue().bitset);
            if (inv != null)
                tree.add(PredicateSetFactory.create(inv.getPredicateSet()).getBitset());
        }
    }

}
