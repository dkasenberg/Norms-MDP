/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

import java.util.*;
import rabinizer.bdd.Globals;
import rabinizer.formulas.Formula;

/**
 *
 * @author jkretinsky
 */
public class ProductAllSlaves extends Product {

    public ProductAllSlaves(FormulaAutomaton master, Map<Formula, RabinSlave> slaves, Globals globals) {
        super(master, slaves, globals);
    }

    @Override
    protected Set<Formula> relevantSlaves(FormulaState masterState) {
        return allSlaves;
    }

}
