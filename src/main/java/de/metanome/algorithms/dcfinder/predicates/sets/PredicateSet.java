package de.metanome.algorithms.dcfinder.predicates.sets;

import java.util.*;

import ch.javasoft.bitset.BitSetFactory;
import ch.javasoft.bitset.LongBitSet;
import ch.javasoft.bitset.LongBitSet;
import ch.javasoft.bitset.LongBitSet.LongBitSetFactory;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;
import de.metanome.algorithms.dcfinder.predicates.Predicate;

public class PredicateSet implements Iterable<Predicate> {

    private static IndexProvider<Predicate> indexProvider = new IndexProvider<>();

    public static void configure(IndexProvider<Predicate> _indexProvider) {
        indexProvider = _indexProvider;
    }


    private final LongBitSet bitset;

    public PredicateSet() {
        this.bitset = new LongBitSet();
    }

    public PredicateSet(LongBitSet bitset) {
        this.bitset = bitset.clone();
    }

    public PredicateSet(PredicateSet pS) {
        this.bitset = pS.getBitset().clone();
    }


    private PredicateSet InvT1T2;

    public PredicateSet getInvT1T2() {
        if (InvT1T2 != null) return InvT1T2;

        PredicateSet res = PredicateSetFactory.create();
        for (Predicate predicate : this) {
            Predicate sym = predicate.getInvT1T2();
            res.add(sym);
        }
        return InvT1T2 = res;
    }


    public void remove(Predicate predicate) {
        this.bitset.clear(indexProvider.getIndex(predicate));
    }

    public boolean containsPredicate(Predicate predicate) {
        return bitset.get(indexProvider.getIndex(predicate));
    }

    public boolean isSubsetOf(PredicateSet superset) {
        return bitset.isSubSetOf(superset.getBitset());
    }

    public LongBitSet getBitset() {
        return bitset;
    }

    public LongBitSet getLongBitSet() {
        return bitset;
    }

    public void addAll(PredicateSet PredicateBitSet) {
        bitset.or(PredicateBitSet.getBitset());
    }

    public int size() {
        return bitset.cardinality();
    }

    public boolean add(Predicate predicate) {
        int index = indexProvider.getIndex(predicate);
        boolean newAdded = !bitset.get(index);
        bitset.set(index);
        return newAdded;
    }

    @Override
    public Iterator<Predicate> iterator() {
        return new Iterator<>() {
            private int currentIndex = bitset.nextSetBit(0);

            @Override
            public Predicate next() {
                int lastIndex = currentIndex;
                currentIndex = bitset.nextSetBit(currentIndex + 1);
                return indexProvider.getObject(lastIndex);
            }

            @Override
            public boolean hasNext() {
                return currentIndex >= 0;
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PredicateSet other = (PredicateSet) obj;
        if (bitset == null)
            return other.bitset == null;
        else
            return bitset.equals(other.bitset);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime + ((bitset == null) ? 0 : bitset.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.forEach(p -> sb.append(p + " "));
        return sb.toString();
    }

}
