package rabinizer.formulas;

import net.sf.javabdd.BDD;
import rabinizer.exec.*;
import rabinizer.bdd.BDDForFormulae;

public class Negation extends FormulaUnary {

    @Override
    public String operator() {
        return "!";
    }

    public Negation(Formula f) {
        super(f);
    }
    
    public Formula copy() {
        return new Negation(operand);
    }
    
    
    public Formula applyParam(String k, String v) {
        return new Negation(operand.applyParam(k, v));
    }

    public Negation ThisTypeUnary(Formula operand) {
        return new Negation(operand);
    }

    public BDD bdd() {            // negation of ATOMIC PROPOSITIONS only
        if (cachedBdd == null) {
            Formula booleanAtom = new Negation(operand.representative());
            int bddVar = globals.bddForFormulae.bijectionBooleanAtomBddVar.id(booleanAtom);
            if (globals.bddForFormulae.bddFactory.varNum() <= bddVar) {
                globals.bddForFormulae.bddFactory.extVarNum(1);
            }
            cachedBdd = globals.bddForFormulae.bddFactory.ithVar(bddVar);
            globals.bddForFormulae.representativeOfBdd(cachedBdd, this);
        }
        return cachedBdd;
    }
    /*
     @Override
     public int hashCode() {
     return operand.hashCode() * 2;
     }

     @Override
     public boolean equals(Object o) {
     if (!(o instanceof Negation)) {
     return false;
     } else {
     return ((Negation) o).operand.equals(this.operand);
     }
     }

     @Override
     public Formula negated() {
     return new Negation(operand.negated());
     }

     @Override
     public String toReversePolishString() {
     return "! " + operand.toReversePolishString();
     }

     @Override
     public String toString() {
     if (cachedString == null) {
     cachedString = "! " + operand.toString();
     }
     return cachedString;
     }

     @Override
     public Tuple<Set<Formula>, Set<Formula>> recurrentFormulas(boolean accumulate) {
     return operand.recurrentFormulas(accumulate);
     }

     @Override
     public Formula evaluateValuation(Valuation valuation) {
     System.out.println("neg"+this);
     Formula o = operand.evaluateValuation(valuation);
     if (!(o instanceof BooleanConstant)) {
     Rabinizer.errorMessageAndExit("Negation.evaluateCurrentAssertions: the given formula is not in Negation Normal Form.");
     }
     return o.negated();
     }

     @Override
     public Formula removeConstants() {
     return this;
     }

     @Override
     public Formula removeX() {
     return operand;
     }
     */

    @Override
    public Formula unfold() {
        throw new UnsupportedOperationException("Supported for NNF only.");
    }

    @Override
    public Formula unfoldNoG() {
        throw new UnsupportedOperationException("Supported for NNF only.");
    }
    /*
     @Override
     public boolean isProgressFormula() {
     return operand.isProgressFormula();
     }

     @Override
     public boolean hasSubformula(Formula f) {
     return this.equals(f) || operand.hasSubformula(f);
     }

     @Override
     public Formula removeXsFromCurrentBooleanAtoms() {
     if (operand.isProgressFormula()) {
     return this;
     } else {
     return operand.removeXsFromCurrentBooleanAtoms();
     }
     }

     @Override
     public Set<Formula> gSubformulas() {
     return operand.gSubformulas();
     }

     @Override
     public Set<Formula> argumentsFinsideG(boolean acc) {
     return operand.argumentsFinsideG(acc);
     }

     public boolean untilOcurrs() {
     return operand.untilOcurrs();
     }
     */

    @Override
    public Formula toNNF() {
        return operand.negationToNNF();
    }

    @Override
    public Formula negationToNNF() {
        return operand.toNNF();
    }
    /*   
     @Override
     public Set<Formula> topmostGs() {
     return operand.topmostGs();
     }
     */
    /*
     public Formula toNNF() {
     if (operand instanceof Literal) {
     Literal l = new Literal((Literal) operand);
     l.negated = !l.negated;
     return l;
     } else if (operand instanceof BooleanConstant) {
     return new BooleanConstant(!((BooleanConstant) operand).value);
     } else if (operand instanceof FOperator) {
     FOperator f = (FOperator) operand;
     return new GOperator((new Negation(f.operand)).toNNF());
     } else if (operand instanceof GOperator) {
     GOperator g = (GOperator) operand;
     return new FOperator((new Negation(g.operand)).toNNF());
     } else if (operand instanceof XOperator) {
     Negation n = (Negation) operand;
     return new XOperator((new Negation(n.operand)).toNNF());
     } else if (operand instanceof Negation) {
     return operand;
     } else if (operand instanceof UOperator) {
     UOperator u = (UOperator) operand;
     Formula l = (new UOperator(new Negation(u.right), new Conjunction(new Negation(u.left), new Negation(u.right)))).toNNF();
     Formula r = (new GOperator(new Negation(u.right))).toNNF();
     return new Disjunction(l, r);
     } else if (operand instanceof Conjunction) {
     Conjunction c = (Conjunction) operand;
     Formula l = (new Negation(c.left)).toNNF();
     Formula r = (new Negation(c.right)).toNNF();
     return new Disjunction(l, r);
     } else if (operand instanceof Disjunction) {
     Disjunction d = (Disjunction) operand;
     Formula l = (new Negation(d.left)).toNNF();
     Formula r = (new Negation(d.right)).toNNF();
     return new Conjunction(l, r);
     } else {
     throw new Error("Negation.toNNF: Unknown formula: " + operand);
     }
     }
     */

}
