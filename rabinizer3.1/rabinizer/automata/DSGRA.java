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

/**
 *
 * @author jkretinsky
 */
public class DSGRA extends Automaton<ProductAccState> implements AccAutomatonInterface {

    DTGRARaw dtgra;
    AccTGR accTGR;
    AccSGR accSGR;

    public DSGRA(DTGRARaw dtgra) {
        super(dtgra.globals);
        this.dtgra = dtgra;
        accTGR = new AccTGR(dtgra.accTGR);
        generate();
        accSGR = new AccSGR(accTGR, this);
    }

    @Override
    protected ProductAccState generateInitialState() {
        Map<Integer, Set<Integer>> accSets = new HashMap();
        for (int i = 0; i < accTGR.size(); i++) {
            accSets.put(i, new HashSet());
        }
        return new ProductAccState(dtgra.automaton.initialState, accSets);
    }

    @Override
    protected ProductAccState generateSuccState(ProductAccState s, ValuationSet vs) {
        Valuation v = vs.pickAny();
        Map<Integer, Set<Integer>> accSets = new HashMap();
        for (int i = 0; i < accTGR.size(); i++) {
            accSets.put(i, new HashSet());
            GRabinPairT grp = accTGR.get(i);
            if (grp.left != null && grp.left.get(s.left) != null && grp.left.get(s.left).contains(v)) {
                accSets.get(i).add(-1);
            }
            for (int j = 0; j < grp.right.size(); j++) {
                if (grp.right.get(j).get(s.left) != null && grp.right.get(j).get(s.left).contains(v)) {
                    accSets.get(i).add(j);
                }
            }
        }
        return new ProductAccState(dtgra.automaton.succ(s.left, v), accSets);
    }

    @Override
    protected Set<ValuationSet> generateSuccTransitions(ProductAccState s) {
        return globals.aV.allValuationsAsSets; // TODO symbolic
    }

    @Override
    public String acc() {
        return accSGR.toString();
    }

    @Override
    protected String accName() {
        String result = "acc-name: generalized-Rabin " + accSGR.size();
        for (int i = 0; i < accSGR.size(); i++) {
            result += " " + (accSGR.get(i).right.size());
        }
        return result + "\n";
    }

    @Override
    protected String accTypeNumerical() {
        if (accSGR.size() == 0) {
            return "0 f";
        }
        String result = "";
        int sum = 0;
        for (int i = 0; i < accSGR.size(); i++) {
            result += i == 0 ? "" : " | ";
            result += "Fin(" + sum + ")";
            sum++;
            for (Set<ProductAccState> set : accSGR.get(i).right) {
                result += "&Inf(" + sum + ")";
                sum++;
            }
        }
        return sum + " " + result;
    }

    @Override
    protected String stateAcc(ProductAccState s) {
        return "\n{" + accSGR.accSets(s) + "}";
    }

    @Override
    protected String outTransToHOA(ProductAccState s) {
        String result = "";
        for (ValuationSet vs : transitions.get(s).keySet()) {
            result += "[" + (new MyBDD(vs.toBdd(), true, globals)).BDDtoNumericString() + "] "
                + statesToNumbers.get(transitions.get(s).get(vs)) + "\n";
        }
        return result;
    }

    @Override
    public int pairNumber() {
        return accSGR.size();
    }

}
