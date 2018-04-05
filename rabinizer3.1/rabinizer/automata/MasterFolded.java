/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

/**
 *
 * @author jkretinsky
 */
import java.util.Set;
import rabinizer.bdd.ValuationSet;
import rabinizer.formulas.Formula;

public class MasterFolded extends FormulaAutomaton {

    public MasterFolded(Formula formula) {
        super(formula);
    }

    @Override
    public FormulaState generateInitialState() {
        Formula init = formula.representative();
        return new FormulaState(init, init);
    }

    @Override
    public FormulaState generateSuccState(FormulaState s, ValuationSet vs) {
        Formula succ = s.formula.unfold().temporalStep(vs.pickAny()).representative(); // any element of the equivalence class
        return new FormulaState(succ, succ);
    }
    
    @Override
    protected Set<ValuationSet> generateSuccTransitions(FormulaState s) {
        return generatePartitioning(s.formula.unfold());
    }

}
