/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.bdd;

import net.sf.javabdd.*;
import rabinizer.formulas.*;

/**
 *
 * @author jkretinsky
 */
public class MyBDD {

    public BDD bdd;
    public boolean valuationType; // true for valuations, false for general formulae
    public Globals globals;

    public MyBDD(BDD bdd, boolean valuationType, Globals globals) {
        this.bdd = bdd;
        this.valuationType = valuationType;
        this.globals = globals;
    }

    public MyBDD and(MyBDD m) {
        return new MyBDD(this.bdd.and(m.bdd), valuationType && m.valuationType, globals);
    }

    public MyBDD or(MyBDD m) {
        return new MyBDD(this.bdd.or(m.bdd), valuationType && m.valuationType, globals);
    }

    public String BDDtoString() { // FIXME!!! uzavorkovani // instead toNumerical and map variableToString 
        if (bdd.isOne()) {
            return "tt";
        } else if (bdd.isZero()) {
            return "ff";
        } else {
            boolean firstOp = false;
            String result = "";
            if (bdd.high().isOne()) {
                result += variableToString(bdd.level()) + "";
                firstOp = true;
            } else if (!bdd.high().isZero()) {
                result += "(" + variableToString(bdd.level()) + "&"
                    + new MyBDD(bdd.high(), valuationType, globals).BDDtoString() + ")";
                firstOp = true;
            }

            if (bdd.low().isOne()) {
                result += (firstOp ? "+" : "") + "!"
                    + variableToString(bdd.level()) + "";
            } else if (!bdd.low().isZero()) {
                result += (firstOp ? "+" : "") + "(!"
                    + variableToString(bdd.level()) + "&"
                    + new MyBDD(bdd.low(), valuationType, globals).BDDtoString() + ")";
            }
            result += "";
            return result;
        }
    }

    public String BDDtoNumericString() {
        if (bdd.isOne()) {
            return "t";
        } else if (bdd.isZero()) {
            return "f";
        } else {
            String result1 = "";
            if (bdd.high().isOne()) {
                result1 += bdd.level();
            } else if (!bdd.high().isZero()) {
                result1 += bdd.level() + "&" + new MyBDD(bdd.high(), valuationType, globals).BDDtoNumericString();
            }

            String result2 = "";
            if (bdd.low().isOne()) {
                result2 = "!" + bdd.level();
            } else if (!bdd.low().isZero()) {
                result2 = "!" + bdd.level() + "&" + new MyBDD(bdd.low(), valuationType, globals).BDDtoNumericString();
            }
            if (result1.isEmpty() || result2.isEmpty()) {
                return result1 + result2;
            } else {
                return "(" + result1 + "|" + result2 + ")";
            }
        }
    }

    public Formula BDDtoFormula() {
        if (bdd.isOne()) {
            return new BooleanConstant(true, globals);
        } else if (bdd.isZero()) {
            return new BooleanConstant(false, globals);
        } else if (bdd.high().equals(bdd.low())) {
            return new MyBDD(bdd.high(), valuationType, globals).BDDtoFormula();
        } else {
            Formula high, low;
            if (bdd.high().isOne()) {
                high = variableToFormula(bdd.level());
            } else if (bdd.high().isZero()) {
                high = new BooleanConstant(false, globals);
            } else {
                high = new Conjunction(variableToFormula(bdd.level()), new MyBDD(bdd.high(), valuationType, globals).BDDtoFormula());
            }

            Formula neg;
            if (variableToFormula(bdd.level()) instanceof Literal) {
                neg = ((Literal) variableToFormula(bdd.level())).negated();
            } else {
                neg = new Negation(variableToFormula(bdd.level()));
            }
            if (bdd.low().isOne()) {
                low = neg;
            } else if (bdd.low().isZero()) {
                low = new BooleanConstant(false, globals);
            } else {
                low = new Conjunction(neg, new MyBDD(bdd.low(), valuationType, globals).BDDtoFormula());
            }

            if (high.equals(new BooleanConstant(false, globals))) {
                return low;
            } else if (low.equals(new BooleanConstant(false, globals))) {
                return high;
            } else {
                return new Disjunction(high, low);
            }
        }
    }

    public String variableToString(int var) {
        return variableToFormula(var).toString();
    }

    public Formula variableToFormula(int var) {
        if (valuationType) {
            return new Literal(globals.bddForVariables.bijectionIdAtom.atom(var), var, false, globals);
        } else {
            return globals.bddForFormulae.bijectionBooleanAtomBddVar.atom(var);
        }
    }
}
