package de.metanome.algorithms.dcfinder.predicates;

import java.util.HashMap;
import java.util.Map;

import de.metanome.algorithms.dcfinder.predicates.Operator;
import de.metanome.algorithms.dcfinder.predicates.operands.ColumnOperand;


public class PredicateProvider {

    private final Map<Operator, Map<ColumnOperand<?>, Map<ColumnOperand<?>, Predicate>>> predicates;

    public PredicateProvider() {
        predicates = new HashMap<>();
    }

    public Predicate getPredicate(Operator op, ColumnOperand<?> op1, ColumnOperand<?> op2) {
        Map<ColumnOperand<?>, Predicate> map = predicates.computeIfAbsent(op, a -> new HashMap<>()).computeIfAbsent(op1, a -> new HashMap<>());
        Predicate p = map.get(op2);
        if (p == null) {
            p = new Predicate(op, op1, op2);
            map.put(op2, p);
        }
        return p;
    }

}
