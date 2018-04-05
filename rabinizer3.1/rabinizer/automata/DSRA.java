/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

import java.util.*;
import rabinizer.bdd.AllValuations;
import rabinizer.bdd.MyBDD;
import rabinizer.bdd.Valuation;
import rabinizer.bdd.ValuationSet;
import rabinizer.bdd.ValuationSetBDD;

/**
 *
 * @author jkretinsky
 */
public class DSRA extends Automaton<ProductDegenAccState> implements AccAutomatonInterface {

    DTRA dtra;
    AccTR accTR;
    Map<ProductDegenState, Set<Integer>> stateAcceptance;
    public AccSR accSR;

    public DSRA(DTRA dtra) {
        super(dtra.globals);
        this.dtra = dtra;
        this.accTR = dtra.accTR;
        stateAcceptance = new HashMap();
        for (ProductDegenState s : dtra.states) {
            stateAcceptance.put(s, new HashSet());
            for (int i = 0; i < accTR.size(); i++) {
                RabinPair<ProductDegenState> rp = accTR.get(i);
                if (globals.vsBDD.getAllVals().equals(rp.left.get(s))) {
                    stateAcceptance.get(s).add(2 * i);
                } else if (globals.vsBDD.getAllVals().equals(rp.right.get(s))) {
                    stateAcceptance.get(s).add(2 * i + 1);
                }
            }
        }
        generate();
        accSR = new AccSR(accTR, this);
    }

    @Override
    protected ProductDegenAccState generateInitialState() {
        return new ProductDegenAccState(dtra.initialState, stateAcceptance.get(dtra.initialState));
    }

    @Override
    protected ProductDegenAccState generateSuccState(ProductDegenAccState s, ValuationSet vs) {
        Valuation v = vs.pickAny();
        ProductDegenState succ = dtra.succ(s.left, v);
        Set<Integer> accSets = new HashSet(stateAcceptance.get(succ));
        for (int i = 0; i < accTR.size(); i++) {
            RabinPair<ProductDegenState> rp = accTR.get(i);
            if (rp.left != null && rp.left.get(s.left) != null && rp.left.get(s.left).contains(v)
                && !stateAcceptance.get(s.left).contains(2 * i)) { // acceptance dealt with already in s
                accSets.add(2 * i);
            }
            if (rp.right != null && rp.right.get(s.left) != null && rp.right.get(s.left).contains(v)
                && !stateAcceptance.get(s.left).contains(2 * i + 1)) {
                accSets.add(2 * i + 1);
            }
            if (accSets.contains(2 * i) && accSets.contains(2 * i + 1)) {
                accSets.remove(2 * i + 1);
            }
        }
        return new ProductDegenAccState(succ, accSets);
    }

    @Override
    protected Set<ValuationSet> generateSuccTransitions(ProductDegenAccState s) {
        return globals.aV.allValuationsAsSets; // TODO symbolic
    }

    @Override
    public String acc() {
        return accSR.toString();
    }
    
    @Override
    protected String accName() {
        return "acc-name: Rabin " + accSR.size() + "\n";
    }

    @Override
    protected String accTypeNumerical() {
        if (accSR.size() == 0) {
            return "0 f";
        }
        String result = accSR.size() * 2 + " ";
        for (int i = 0; i < accSR.size(); i++) {
            result += i == 0 ? "" : " | ";
            result += "Fin(" + 2 * i + ")&Inf(" + (2 * i + 1) + ")";
        }
        return result;
    }

    @Override
    protected String stateAcc(ProductDegenAccState s) {
        return accSR.accSets(s);
    }

    @Override
    protected String outTransToHOA(ProductDegenAccState s) {
        String result = "";
        for (ValuationSet vs : transitions.get(s).keySet()) {
            result += "[" + (new MyBDD(vs.toBdd(),true, globals)).BDDtoNumericString() + "] " 
                + statesToNumbers.get(transitions.get(s).get(vs)) + "\n";
        }
        return result;
    }
    
    @Override
    public int pairNumber() {
        return accSR.size() / 2;
    }
}
