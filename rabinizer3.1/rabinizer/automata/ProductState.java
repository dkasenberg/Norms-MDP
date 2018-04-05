/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

import java.util.HashMap;
import rabinizer.formulas.Formula;

/**
 *
 * @author jkretinsky
 */
public class ProductState extends HashMap<Formula, RankingState> {

    public FormulaState masterState;

    public ProductState(FormulaState masterState) {
        super();
        this.masterState = masterState;
    }

    @Override
    public int hashCode() {
        return 17 * super.hashCode() + 5 * masterState.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        } else {
            return ((ProductState) o).masterState.equals(this.masterState);
        }
    }

    public String toString() {
        String result = masterState + "::";
        for (Formula slave : keySet()) {
            result += get(slave) + ";";
        }
        return result;//masterState + "::" + super.toString();
    }

}
