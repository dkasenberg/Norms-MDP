/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.rabin;

import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import normativeagents.parsing.ParseException;
import rabinizer.automata.DSRA;
import rabinizer.automata.DTGRARaw;
import rabinizer.automata.DTRA;
import rabinizer.formulas.Formula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 *
 * @author dkasenberg
 */
public class RabinAutomaton {
    public List<FormulaVar> formulaVars;
    public Formula formula;
    public DSRA dsra;
    
    public RabinAutomaton(Formula formula, Domain d, State s) throws ParseException {
        this.formula = formula;
        this.dsra = computeAutomaton();
    }
    
    
    public class FormulaVar {
        public String name;
        public String[] params;
        
        public FormulaVar(String origName) {
            String[] v = origName.split("[:,]");
            name = v[0];
            params = Arrays.copyOfRange(v, 1, v.length);
        }
        
        @Override
        public String toString() {
            return name + "(" + String.join(",", params) + ")";
        }
        
    }
    
    
    public final DSRA computeAutomaton() throws ParseException {
        //nonsilent("Input formula: " + formula);
        
        formulaVars = new ArrayList<>();
        
        for(String varName : formula.globals.bddForVariables.variables) {
            formulaVars.add(new FormulaVar(varName));
        }

        boolean unfoldedOn = false;
        boolean sinksOn = true;
        boolean optimizeInitialStatesOn = true;
        boolean relevantSlavesOnlyOn = true;
        boolean slowerIsabelleAccForUnfolded = false;
        
        

//        if (!optimize) {
//            unfoldedOn = false;
//            optimizeInitialStatesOn = false;
//            relevantSlavesOnlyOn = false;
//        }

        //DGRA dgra = new DTGRA(phi); for optimized
        DTGRARaw dtgra = new DTGRARaw(formula.toNNF(), true, unfoldedOn, sinksOn, optimizeInitialStatesOn, relevantSlavesOnlyOn, slowerIsabelleAccForUnfolded);
        return new DSRA(new DTRA(dtgra));
    }
    
    
    
}
