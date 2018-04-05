/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

import java.util.Map;
import rabinizer.bdd.ValuationSet;
import rabinizer.exec.Main;
import rabinizer.exec.Tuple;

/**
 *
 * @author jkretinsky
 * @param <State>
 */
public class RabinPair<State> extends Tuple<TranSet<State>, TranSet<State>> {

    public RabinPair(TranSet<State> l, TranSet<State> r) {
        super(l, r);
    }
    
    public RabinPair(RabinPair<State> rp){
        super(rp.left,rp.right);
    }
    
    public RabinPair(RabinSlave slave, Map<FormulaState, Boolean> finalStates, int rank, Product product){
        this(fromSlave(slave, finalStates, rank, product));
    }
    
    @Override
    public String toString() {
        return "Fin:\n" + (left == null ? "trivial" : left) + "\nInf:\n" + (right == null ? "trivial" : right);
    }
    
    private static RabinPair fromSlave(RabinSlave slave, Map<FormulaState, Boolean> finalStates, int rank, Product product) {

        // Set fail
        // Mojmir
        TranSet<FormulaState> failM = new TranSet();
        for (FormulaState fs : slave.mojmir.states) {
            //if (!slave.mojmir.sinks.contains(fs)) {
            for (Map.Entry<ValuationSet, FormulaState> vsfs : slave.mojmir.transitions.get(fs).entrySet()) {
                if (slave.mojmir.sinks.contains(vsfs.getValue()) && !finalStates.get(vsfs.getValue())) {
                    failM.add(fs, vsfs.getKey());
                }
            }
            //}
        }
        // Product
        TranSet<ProductState> failP = new TranSet();
        for (ProductState ps : product.states) {
            RankingState rs = ps.get(slave.mojmir.formula);
            if (rs != null) { // relevant slave
                for (FormulaState fs : rs.keySet()) {
                    if (failM.containsKey(fs)) {
                        failP.add(ps, failM.get(fs));
                    }
                }
            }
        }

        // Set succeed(pi)
        // Mojmir
        TranSet<FormulaState> succeedM = new TranSet();
        if (finalStates.get(slave.mojmir.initialState)) {
            for (FormulaState fs : slave.mojmir.states) {
                for (Map.Entry<ValuationSet, FormulaState> vsfs : slave.mojmir.transitions.get(fs).entrySet()) {
                    succeedM.add(fs, vsfs.getKey());
                }
            }
        } else {
            for (FormulaState fs : slave.mojmir.states) {
                if (!finalStates.get(fs)) {
                    for (Map.Entry<ValuationSet, FormulaState> vsfs : slave.mojmir.transitions.get(fs).entrySet()) {
                        if (finalStates.get(vsfs.getValue())) {
                            succeedM.add(fs, vsfs.getKey());
                        }
                    }
                }
            }
        }
        // Product
        TranSet<ProductState> succeedP = new TranSet();
        for (ProductState ps : product.states) {
            RankingState rs = ps.get(slave.mojmir.formula);
            if (rs != null) { // relevant slave
                for (FormulaState fs : rs.keySet()) {
                    if (succeedM.containsKey(fs) && (rs.get(fs) == rank)) {
                        succeedP.add(ps, succeedM.get(fs));
                    }
                }
            }
        }
        // Set buy(pi)
        // Rabin
        TranSet<RankingState> buyR = new TranSet();
        for (RankingState rs : slave.states) {
            for (FormulaState fs : rs.keySet()) {
                if (rs.get(fs) < rank) {
                    for (FormulaState fs2 : rs.keySet()) {
                        for (FormulaState succ : slave.mojmir.states) {
                            ValuationSet vs1, vs2;
                            if (!finalStates.get(succ)
                                && ((vs1 = slave.mojmir.edgeBetween.get(new Tuple(fs, succ))) != null)
                                && ((vs2 = slave.mojmir.edgeBetween.get(new Tuple(fs2, succ))) != null)) {
                                if (!fs.equals(fs2)) {
                                    buyR.add(rs, vs1.and(vs2));
                                } else if (succ.equals(slave.mojmir.initialState)) {
                                    buyR.add(rs, vs1);
                                }

                            }
                        }
                    }
                }
            }
        }
        // Product
        TranSet<ProductState> buyP = new TranSet();
        for (ProductState ps : product.states) {
            RankingState rs = ps.get(slave.mojmir.formula);
            if (rs != null) { // relevant slave
                if (buyR.containsKey(rs)) {
                    buyP.add(ps, buyR.get(rs));
                }
            }
        }
        /*
         TranSet forbidden = new TranSet();
         forbidden.addAll(failP).addAll(buyP);
         TranSet required = new TranSet();
         forbidden.addAll(succeedP);
         */
        Main.verboseln("\tAn acceptance pair for slave " + slave.mojmir.formula + ":\n" + failP + buyP + succeedP);
        return new RabinPair(failP.addAll(buyP), succeedP);
    }

    

}
