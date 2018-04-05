package rabinizer.formulas;

import java.util.HashSet;
import java.util.Set;
import rabinizer.bdd.GSet;

public class GOperator extends FormulaUnary {

    @Override
    public String operator() {
        return "G";
    }

    public GOperator(Formula f) {
        super(f);
    }
    
    public Formula copy() {
        return new GOperator(operand);
    }
    
    public Formula applyParam(String k, String v) {
        return new GOperator(operand.applyParam(k, v));
    }

    @Override
    public GOperator ThisTypeUnary(Formula operand) {
        return new GOperator(operand);
    }

    @Override
    public Formula unfold() {
        // U(F phi) = U(phi) \/ X F U(phi)
        return new Conjunction(operand.unfold(), /*new XOperator*/ (this));
    }

    @Override
    public Formula unfoldNoG() {
        return this;
    }

    @Override
    public Formula toNNF() {
        return new GOperator(operand.toNNF());
    }

    @Override
    public Formula negationToNNF() {
        return new FOperator(operand.negationToNNF());
    }

    //============== OVERRIDE ====================
    @Override
    public boolean containsG() {
        return true;
    }

    @Override
    public Set<Formula> gSubformulas() {
        Set<Formula> r = operand.gSubformulas();
        r.add(operand);
        return r;
    }

    @Override
    public Set<Formula> topmostGs() {
        Set<Formula> result = new HashSet();
        result.add(this.operand);
        return result;
    }

    @Override
    public Formula substituteGsToFalse(GSet gSet) {
        if (gSet.contains(operand)) {
            return new BooleanConstant(false, globals);
        } else {
            return this;
        }
    }

}
