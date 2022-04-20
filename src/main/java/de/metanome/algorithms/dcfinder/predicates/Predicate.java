package de.metanome.algorithms.dcfinder.predicates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


import de.metanome.algorithms.dcfinder.predicates.Operator;
import de.metanome.algorithms.dcfinder.predicates.operands.ColumnOperand;


public class Predicate {
    private static PredicateProvider predicateProvider;

    public static void configure(PredicateProvider provider) {
        predicateProvider = provider;
    }


    private final Operator op;
    private final ColumnOperand operand1;
    private final ColumnOperand operand2;

    public Predicate(Operator op, ColumnOperand<?> operand1, ColumnOperand<?> operand2) {
        if (op == null)
            throw new IllegalArgumentException("Operator must not be null.");

        if (operand1 == null)
            throw new IllegalArgumentException("First operand must not be null.");

        if (operand2 == null)
            throw new IllegalArgumentException("Second operand must not be null.");

        this.op = op;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    private Predicate symmetric;

    public Predicate getSymmetric() {
        if (symmetric != null) return symmetric;
        return symmetric = predicateProvider.getPredicate(op.getSymmetric(), operand2, operand1);
    }

    private Predicate operatorSymmetric;

    public Predicate getOperatorSymmetric() {
        if (operatorSymmetric == null)
            operatorSymmetric = predicateProvider.getPredicate(op.getSymmetric(), operand1, operand2);
        return operatorSymmetric;
    }


    private Predicate InvT1T2;

    public Predicate getInvT1T2() {
        if (InvT1T2 != null) return InvT1T2;
        return InvT1T2 = predicateProvider.getPredicate(op, operand1.getInvT1T2(), operand2.getInvT1T2());
    }

    private Predicate inverse;

    public Predicate getInverse() {
        if (inverse != null) return inverse;
        return inverse = predicateProvider.getPredicate(op.getInverse(), operand1, operand2);
    }

    private List<Predicate> implications = null;

    public Collection<Predicate> getImplications() {
        if (this.implications != null) return implications;
        Operator[] opImplications = op.getImplications();

        List<Predicate> implications = new ArrayList<>(opImplications.length);
        for (Operator opImplication : opImplications) {
            implications.add(predicateProvider.getPredicate(opImplication, operand1, operand2));
        }
        this.implications = Collections.unmodifiableList(implications);
        return implications;
    }

    public boolean implies(Predicate add) {
        if (add.operand1.equals(this.operand1) && add.operand2.equals(this.operand2))
            for (Operator i : op.getImplications())
                if (add.op == i)
                    return true;
        return false;
    }

    public Operator getOperator() {
        return op;
    }

    public ColumnOperand<?> getOperand1() {
        return operand1;
    }

    public ColumnOperand<?> getOperand2() {
        return operand2;
    }

    public boolean satisfies(int line1, int line2) {
        return op.eval(operand1.getValue(line1, line2), operand2.getValue(line1, line2));
    }

    @Override
    public String toString() {
        //return "[Predicate: " + operand1.toString() + " " + op.getShortString() + " " + operand2.toString() + "]";
        return operand1 + " " + op.getShortString() + " " + operand2;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + op.hashCode();
        result = prime * result + operand1.hashCode();
        result = prime * result + operand2.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        Predicate other = (Predicate) obj;
        if (op != other.op)
            return false;
        if (operand1 == null) {
            if (other.operand1 != null)
                return false;
        } else if (!operand1.equals(other.operand1))
            return false;

        if (operand2 == null)
            return other.operand2 == null;
        else
            return operand2.equals(other.operand2);
    }

    public boolean isCrossColumn() {
        return !operand1.getColumn().getColumnIdentifier().equals(operand2.getColumn().getColumnIdentifier());
    }
}
