package de.metanome.algorithms.dcfinder.predicates.sets;

import de.metanome.algorithms.dcfinder.predicates.Predicate;


public class PredicateSetFactory {

    public static PredicateSet create(Predicate... predicates) {
        PredicateSet set = new PredicateSet();
        for (Predicate p : predicates)
            set.add(p);
        return set;
    }

    public static PredicateSet create(PredicateSet pS) {
        return new PredicateSet(pS);
    }

}
