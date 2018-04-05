/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.formulas;

import java.util.List;
import java.util.Set;
import net.sf.javabdd.BDD;
import rabinizer.bdd.Valuation;

/**
 *
 * @author jkretinsky
 */
public abstract class FormulaBinary extends Formula {

    public Formula left, right;
    
    public FormulaBinary(Formula left, Formula right) {
        super(left.globals);
        this.left = left;
        this.right = right;
    }

    @Override
    public int hashCode() {
        return 7 * left.hashCode() + 5 * right.hashCode() + operator().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FormulaBinary)) {
            return false;
        } else {
            return o.getClass().equals(getClass()) && ((FormulaBinary) o).left.equals(left) && ((FormulaBinary) o).right.equals(right);
        }
    }
    
    @Override
    public String toString() {
        if (cachedString == null) {
            cachedString = "(" + left.toString() + " " + operator() + " " + right.toString() + ")";
        }
        return cachedString;
    }

    @Override
    public String toReversePolishString() {
        return operator() + " " + left.toReversePolishString() + " " + right.toReversePolishString();
    }
    
    @Override
    public boolean containsG() {
        return left.containsG() || right.containsG();
    }

    @Override
    public boolean hasSubformula(Formula f) {
        return this.equals(f) || left.hasSubformula(f) || right.hasSubformula(f);
    }

    @Override
    public Set<Formula> gSubformulas() {
        Set<Formula> r = left.gSubformulas();
        r.addAll(right.gSubformulas());
        return r;
    }

    @Override
    public Set<Formula> topmostGs() {
        Set<Formula> result = left.topmostGs();
        result.addAll(right.topmostGs());
        return result;
    }

}
