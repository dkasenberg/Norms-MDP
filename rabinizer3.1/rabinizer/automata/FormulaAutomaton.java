/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

import java.util.*;
import rabinizer.bdd.BDDForVariables;
import rabinizer.bdd.ValuationSet;
import rabinizer.bdd.ValuationSetBDD;
import rabinizer.formulas.Formula;
import rabinizer.formulas.Literal;

/**
 *
 * @author jkretinsky
 */
public abstract class FormulaAutomaton extends Automaton<FormulaState> {

    public Formula formula;
    protected Map<FormulaState, Formula> stateLabels;

    public FormulaAutomaton(Formula formula) {
        super(formula.globals);
        this.formula = formula;
        stateLabels = new HashMap();
    }

    @Override
    protected Set<ValuationSet> generateSuccTransitions(FormulaState s) {
        return generatePartitioning(s.formula);
    }
    
    protected Set<ValuationSet> generatePartitioning(Formula f) { // TODO method of state
        Set<ValuationSet> result = new HashSet();
        Literal l = f.getAnUnguardedLiteral();
        if (l == null) {
            result.add(new ValuationSetBDD(globals.bddForVariables.getTrueBDD(), globals));
        } else {
            l = l.positiveLiteral();
            //System.out.println("  gen " + f + "; " + l);
            Set<ValuationSet> pos = generatePartitioning(f.assertLiteral(l));
            Set<ValuationSet> neg = generatePartitioning(f.assertLiteral(l.negated()));
            for (ValuationSet vs : pos) {
                result.add(vs.and(l));
            }
            for (ValuationSet vs : neg) {
                result.add(vs.and(l.negated()));
            }
        }
        return result;
    }

}
