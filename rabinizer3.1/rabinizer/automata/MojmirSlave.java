/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

import rabinizer.bdd.*;
import rabinizer.formulas.*;

/**
 *
 * @author jkretinsky
 */
public class MojmirSlave extends FormulaAutomaton {

    public MojmirSlave(Formula formula) {
        super(formula);
        //this.formula = new GOperator(formula);
    }

    @Override
    public FormulaState generateInitialState() {
        FormulaState init = new FormulaState(formula.unfoldNoG().representative(), formula);
        stateLabels.put(init, formula);
        return init;
    }

    @Override
    public FormulaState generateSuccState(FormulaState s, ValuationSet vs) {
        Formula label = s.formula.temporalStep(vs.pickAny()); // any element of the equivalence class
        FormulaState state = new FormulaState(label.unfoldNoG().representative(), label); 
        if (states.contains(state)) {
            state.label = stateLabels.get(state);
        } else {
            stateLabels.put(state, label);
        }
        return state;
    }

}
