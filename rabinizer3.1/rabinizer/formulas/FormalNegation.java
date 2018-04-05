/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.formulas;

import net.sf.javabdd.BDD;
import rabinizer.deleteOld.Misc;
import rabinizer.bdd.BDDForFormulae;

/**
 *
 * @author jkretinsky
 */
public class FormalNegation extends Negation {

    public FormalNegation(Formula f) {
        super(f);
    }
    
    public Formula copy() {
        return new FormalNegation(operand);
    }
    
    public Formula applyParam(String k, String v) {
        return new FormalNegation(operand.applyParam(k, v));
    }

    @Override
    public BDD bdd() {            // negation of NON-ATOMIC PROPOSITIONS only
        if (cachedBdd == null) {
            cachedBdd = operand.bdd().not();
            globals.bddForFormulae.representativeOfBdd(cachedBdd, this);
        }
        return cachedBdd;
    }

}
