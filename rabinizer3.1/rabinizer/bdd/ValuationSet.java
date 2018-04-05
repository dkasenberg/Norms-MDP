package rabinizer.bdd;

import rabinizer.automata.*;
import rabinizer.exec.*;
import java.util.*;

import rabinizer.formulas.BooleanConstant;
import rabinizer.formulas.Disjunction;
import rabinizer.formulas.Formula;
import rabinizer.formulas.Literal;
import net.sf.javabdd.*;

public abstract class ValuationSet extends Object {
    
    public Globals globals;
    public ValuationSet(Globals globals) {
        this.globals = globals;
    }

    /**
     * An automatically generated serial version.
     */
    private static final long serialVersionUID = -5648284081889006821L;

    //protected static BDDFactory bf;
    public abstract Formula toFormula();

    public abstract BDD toBdd();

    public abstract Set<Valuation> toSet();

    public abstract Valuation pickAny();

    public abstract ValuationSet add(ValuationSet vs);

    public abstract ValuationSet add(Valuation v);

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

    public abstract boolean isAllVals();

    public abstract boolean contains(Valuation v);

    public abstract boolean contains(ValuationSet vs);

    public abstract boolean isEmpty();

    public abstract ValuationSet and(Literal literal);

    public abstract ValuationSet and(ValuationSet vs);

    public abstract ValuationSet or(ValuationSet vs);

    public abstract ValuationSet complement();

    public abstract void remove(ValuationSet vs);

    @Override
    public String toString() {
        return this.toFormula().toString();
    }
    
}
