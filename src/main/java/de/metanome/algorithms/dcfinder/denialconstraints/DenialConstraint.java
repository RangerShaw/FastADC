package de.metanome.algorithms.dcfinder.denialconstraints;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;
import ch.javasoft.bitset.search.NTreeSearch;
import de.metanome.algorithms.dcfinder.predicates.Operator;
import de.metanome.algorithms.dcfinder.predicates.Predicate;
import de.metanome.algorithms.dcfinder.predicates.sets.Closure;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSetFactory;


public class DenialConstraint {

    private final PredicateSet predicateSet;

    public DenialConstraint(PredicateSet predicateSet) {
        this.predicateSet = predicateSet;
    }

    public DenialConstraint(LongBitSet predicates) {
        this.predicateSet = new PredicateSet(predicates);
    }

    public boolean containsPredicate(Predicate p) {
        return predicateSet.containsPredicate(p) || predicateSet.containsPredicate(p.getSymmetric());
    }

    public DenialConstraint getInvT1T2DC() {
        return new DenialConstraint(predicateSet.getInvT1T2());
    }

    public PredicateSet getPredicateSet() {
        return predicateSet;
    }

    public int getPredicateCount() {
        return predicateSet.size();
    }

    private boolean containedIn(PredicateSet otherPS) {
        for (Predicate p : predicateSet) {
            if (!otherPS.containsPredicate(p) && !otherPS.containsPredicate(p.getSymmetric()))
                return false;
        }
        return true;
    }


    @Override
    public int hashCode() {
        int result1 = 0;
        for (Predicate p : predicateSet)
            result1 += Math.max(p.hashCode(), p.getSymmetric().hashCode());

        int result2 = 0;
        if (getInvT1T2DC() != null)
            for (Predicate p : getInvT1T2DC().predicateSet)
                result2 += Math.max(p.hashCode(), p.getSymmetric().hashCode());

        return Math.max(result1, result2);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        DenialConstraint other = (DenialConstraint) obj;
        if (predicateSet == null) {
            return other.predicateSet == null;
        } else if (predicateSet.size() != other.predicateSet.size()) {
            return false;
        } else {
            PredicateSet otherPS = other.predicateSet;
            return containedIn(otherPS) || getInvT1T2DC().containedIn(otherPS) || containedIn(other.getInvT1T2DC().predicateSet);
        }
    }


    public static final String NOT = "\u00AC";
    public static final String AND = " ∧ ";

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(NOT + "{ ");
        int count = 0;
        for (Predicate predicate : this.predicateSet) {
            if (count == 0)
                sb.append(predicate.toString());
            else
                sb.append(AND + predicate.toString());

            count++;
        }
        sb.append(" }");
        return sb.toString();
    }

}
