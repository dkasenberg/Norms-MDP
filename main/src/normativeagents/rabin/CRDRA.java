/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.rabin;

import normativeagents.Helper;
import normativeagents.NormInstance;
import normativeagents.parsing.LTLNorm;
import rabinizer.automata.DSRA;
import rabinizer.automata.ProductDegenAccState;
import rabinizer.bdd.Valuation;
import rabinizer.formulas.Formula;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author dkasenberg
 */
public class CRDRA {
    public LTLNorm norm;
    public NormInstance normInstance;
    public List<RabinAutomaton.FormulaVar> formulaVars;
    public Formula formula;
    public DSRA dsra;
    public double discount;
    public int ACC;
    public List<Set<Integer>> accSR;
    public int initState;
    public double weight;
    public Map<Integer, ProductDegenAccState> numbersToStates;
    
//    public Map<Triple<Integer, ValuationSet, Integer>, List<Double>> transitions;

    public CRDRA(RabinAutomaton ra, double weight, double discount) {
        this.formulaVars = ra.formulaVars;
        this.formula = ra.formula;
        this.dsra = ra.dsra;
        this.weight = weight;
//        this.transitions = new HashMap<>();
        
        this.initState = dsra.statesToNumbers.get(dsra.initialState);
        
        this.numbersToStates = Helper.reverse(dsra.statesToNumbers);
        
//        System.err.println(dsra.accSR);
        
//        System.err.println(dsra.statesToNumbers);
        
        accSR = dsra.accSR.stream()
                .map(set -> set.stream()
                        .map(s -> dsra.statesToNumbers.get(s))
                        .collect(Collectors.toSet()))
                .collect(Collectors.toList());
//        System.err.println(accSR);
    }
    
    public int succ(int curState, Valuation valuation) {
        return dsra
                .statesToNumbers
                .get(dsra.succ(numbersToStates
                        .get(curState), valuation));
    }
    
}
