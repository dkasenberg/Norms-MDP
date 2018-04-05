/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rabinizer.automata;

import rabinizer.formulas.Formula;

/**
 *
 * @author jkretinsky
 */
public class FormulaState extends Object{  //TODO extends Formula
    
    public Formula formula;
    
    Formula label;
    
    public FormulaState(Formula formula, Formula label){
        this.formula = formula;
        this.label = label;
    }
    
    @Override
     public String toString(){
        return label.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FormulaState)) {
            return false;
        } else {
            return ((FormulaState) o).formula.equals(this.formula);
        }
    }
    
    @Override
    public int hashCode() {
        return formula.hashCode();
    }
    
}
