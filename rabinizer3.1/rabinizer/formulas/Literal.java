package rabinizer.formulas;

import rabinizer.bdd.Valuation;
import net.sf.javabdd.*;
import rabinizer.bdd.BDDForFormulae;
import rabinizer.bdd.Globals;

public class Literal extends FormulaNullary {

    public String atom;
    public int atomId;
    public boolean negated;

    public Literal(String atom, int atomId, boolean negated, Globals globals) {
        super(globals);
        this.atom = atom;
        this.atomId = atomId;
        this.negated = negated;
    }

    @Override
    public String operator() {
        return null;
    }

    public Literal positiveLiteral() {
        return new Literal(this.atom, this.atomId, false, globals);
    }

    public Literal negated() {
        return new Literal(atom, atomId, !negated, globals);
    }
    
    public Formula copy() {
        return new Literal(atom, atomId, negated, globals);
    }

    @Override
    public BDD bdd() { 
        if (cachedBdd == null) { 
            int bddVar = globals.bddForFormulae.bijectionBooleanAtomBddVar.id(this.positiveLiteral()); // R3: just "this"
            if (globals.bddForFormulae.bddFactory.varNum() <= bddVar) {
                globals.bddForFormulae.bddFactory.extVarNum(1);
            }
            cachedBdd = (negated ? globals.bddForFormulae.bddFactory.nithVar(bddVar) : globals.bddForFormulae.bddFactory.ithVar(bddVar));
            globals.bddForFormulae.representativeOfBdd(cachedBdd, this);
        } 
        return cachedBdd;
    }

    @Override
    public int hashCode() {
        return atomId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Literal)) {
            return false;
        } else {
            return ((Literal) o).atomId == this.atomId && ((Literal) o).negated == this.negated;
        }
    }

    @Override
    public String toReversePolishString() {
        return cachedString = (negated ? "! " : "") + atom;
    }

    @Override
    public String toString() {
        if (cachedString == null) {
            cachedString = (negated ? "!" : "") + atom;
        }
        return cachedString;
    }

    @Override
    public Formula evaluateValuation(Valuation valuation) {
        return new BooleanConstant(valuation.get(atomId) ^ negated, globals);
    }

    @Override
    public Formula evaluateLiteral(Literal literal) {
        if (literal.atomId != this.atomId) {
            return this;
        } else {
            return new BooleanConstant(literal.negated == this.negated, globals);
        }
    }

    @Override
    public Formula negationToNNF() {
        return this.negated();
    }

    @Override
    public Literal getAnUnguardedLiteral() {
        return this;
    }

}
