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
import rabinizer.bdd.ValuationSet;
import rabinizer.formulas.Formula;

public class Master extends FormulaAutomaton {

    public Master(Formula formula) {
        super(formula);
    }

    @Override
    public FormulaState generateInitialState() {
        FormulaState init = new FormulaState(formula.unfold().representative(), formula);
        stateLabels.put(init, formula);
        return init;
    }

    @Override
    public FormulaState generateSuccState(FormulaState s, ValuationSet vs) {
        Formula label = s.formula.temporalStep(vs.pickAny()); // any element of the equivalence class
        FormulaState state = new FormulaState(label.unfold().representative(), label);
        if (states.contains(state)) {
            state.label = stateLabels.get(state);
        } else {
            stateLabels.put(state, label);
        }
        return state;
    }

}
