/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.formulas;

import java.util.HashSet;
import java.util.Set;
import rabinizer.bdd.Globals;

/**
 *
 * @author jkretinsky
 */
public abstract class FormulaNullary extends Formula {

    public FormulaNullary(Globals globals) {
        super(globals);
    }

    @Override
    public Formula unfold() {
        return this;
    }

    @Override
    public Formula unfoldNoG() {
        return this;
    }

    @Override
    public boolean hasSubformula(Formula f) {
        return this.equals(f);
    }

    @Override
    public Set<Formula> gSubformulas() {
        return new HashSet();
    }

    @Override
    public Formula toNNF() {
        return this;
    }

    @Override
    public Set<Formula> topmostGs() {
        return new HashSet();
    }

    @Override
    public boolean containsG() {
        return false;
    }

}
