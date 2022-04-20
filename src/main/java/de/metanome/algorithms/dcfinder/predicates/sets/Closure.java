package de.metanome.algorithms.dcfinder.predicates.sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.metanome.algorithms.dcfinder.predicates.Operator;
import de.metanome.algorithms.dcfinder.predicates.Predicate;
import de.metanome.algorithms.dcfinder.predicates.PredicateProvider;


public class Closure {
    private static PredicateProvider predicateProvider;

    public static void configure(PredicateProvider provider) {
        predicateProvider = provider;
    }


    private PredicateSet start;
    private PredicateSet closure;
    private boolean added;
    private Map<Operator, List<Predicate>> grouped;

    public Closure(PredicateSet start) {
        this.start = start;
        this.grouped = new HashMap<>();
        this.closure = PredicateSetFactory.create();
    }

    public boolean construct() {
        for (Predicate p : start) {
            if (!addAll(p.getImplications()))
                return false;
            if (p.getSymmetric() != null && !addAll(p.getSymmetric().getImplications()))
                return false;
        }

        added = true;
        while (added) {
            added = false;
            if (!transitivityStep()) return false;
        }
        return true;
    }

    public PredicateSet getClosure() {
        return closure;
    }

    private boolean transitivityStep() {
        Set<Predicate> additions = new HashSet<>();
        closure.forEach(p -> {
            if (p.getSymmetric() != null)
                additions.addAll(p.getSymmetric().getImplications());
            additions.addAll(p.getImplications());
//			additions.add(predicateProvider.getPredicate(Operator.EQUAL, p.getOperand1(), p.getOperand1()));
//			additions.add(predicateProvider.getPredicate(Operator.EQUAL, p.getOperand2(), p.getOperand2()));
//			additions.add(predicateProvider.getPredicate(Operator.GREATER_EQUAL, p.getOperand1(), p.getOperand1()));
//			additions.add(predicateProvider.getPredicate(Operator.GREATER_EQUAL, p.getOperand2(), p.getOperand2()));
//			additions.add(predicateProvider.getPredicate(Operator.LESS_EQUAL, p.getOperand1(), p.getOperand1()));
//			additions.add(predicateProvider.getPredicate(Operator.LESS_EQUAL, p.getOperand2(), p.getOperand2()));
        });

        for (Entry<Operator, List<Predicate>> entry : grouped.entrySet()) {
            Operator op = entry.getKey();
            List<Predicate> list = entry.getValue();
            for (Operator opTrans : op.getTransitives()) {
                List<Predicate> pTrans = grouped.get(opTrans);
                if (pTrans == null)
                    continue;

                for (Predicate p : list) {
                    for (Predicate p2 : pTrans) {
                        if (p == p2)
                            continue;
                        // A -> B ; B -> C
                        if (p.getOperand2().equals(p2.getOperand1())) {
                            Predicate newPred = predicateProvider.getPredicate(op, p.getOperand1(), p2.getOperand2());
                            additions.add(newPred);
                        }
                        // C -> A ; A -> B
                        if (p2.getOperand2().equals(p.getOperand1())) {
                            Predicate newPred = predicateProvider.getPredicate(op, p2.getOperand1(), p.getOperand2());
                            additions.add(newPred);
                        }
                    }
                }
            }
        }


        List<Predicate> uneqList = grouped.get(Operator.UNEQUAL);
        if (uneqList != null) {
            for (Predicate p : uneqList) {
                if (closure.containsPredicate(predicateProvider.getPredicate(Operator.LESS_EQUAL, p.getOperand1(), p.getOperand2())))
                    additions.add(predicateProvider.getPredicate(Operator.LESS, p.getOperand1(), p.getOperand2()));
                if (closure.containsPredicate(predicateProvider.getPredicate(Operator.GREATER_EQUAL, p.getOperand1(), p.getOperand2())))
                    additions.add(predicateProvider.getPredicate(Operator.GREATER, p.getOperand1(), p.getOperand2()));
            }
        }
        List<Predicate> leqList = grouped.get(Operator.LESS_EQUAL);
        if (leqList != null) {
            for (Predicate p : leqList) {
                if (closure.containsPredicate(predicateProvider.getPredicate(Operator.GREATER_EQUAL, p.getOperand1(), p.getOperand2())))
                    additions.add(predicateProvider.getPredicate(Operator.EQUAL, p.getOperand1(), p.getOperand2()));
            }
        }
        return addAll(additions);
    }

    private boolean addAll(Collection<Predicate> predicates) {
        for (Predicate p : predicates) {
            if (closure.add(p)) {
//				if((p.getOperator() == Operator.GREATER || p.getOperator() == Operator.LESS || p.getOperator() == Operator.UNEQUAL) && p.getOperand1().equals(p.getOperand2()))
//					throw new TrivialPredicateSetException();
                if (closure.containsPredicate(p.getInverse()))
                    return false;
                grouped.computeIfAbsent(p.getOperator(), op -> new ArrayList<>()).add(p);
                added = true;
            }
        }
        return true;
    }

}
