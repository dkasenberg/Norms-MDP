/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

import rabinizer.bdd.GSet;
import java.util.*;
import rabinizer.formulas.*;
import rabinizer.bdd.*;
import rabinizer.exec.Main;
import rabinizer.deleteOld.Misc;

/**
 *
 * @author jkretinsky
 */
public class AccLocal {

    protected final Product product;
    protected final Formula formula;
    protected final Map<Formula, Integer> maxRank = new HashMap();
    final Map<Formula, GSet> topmostGs = new HashMap(); // without the outer G
    final TranSet<ProductState> allTrans = new TranSet();
    // separate automata acceptance projected to the whole product
    Map<Formula, Map<GSet, Map<Integer, RabinPair>>> accSlavesOptions = new HashMap(); 
    Map<GSet, Map<Map<Formula, Integer>, RabinPair>> accMasterOptions = new HashMap(); // actually just coBuchi
    protected Globals globals;
    
    public AccLocal(Product product) {
        this.product = product;
        this.globals = product.globals;
        this.formula = product.master.formula;
        for (Formula f : formula.gSubformulas()) {
            int maxRankF = 0;
            for (RankingState rs : product.slaves.get(f).states) {
                maxRankF = maxRankF >= rs.size() ? maxRankF : rs.size();
            }
            maxRank.put(f, maxRankF);
            topmostGs.put(f, new GSet(f.topmostGs()));
            Map<GSet, Map<Integer, RabinPair>> optionForf = computeAccSlavesOptions(f);
            accSlavesOptions.put(f, optionForf);
        }
        Main.verboseln("Acceptance for slaves:\n" + this.accSlavesOptions);
        ValuationSet allVals = new ValuationSetBDD(globals.bddForVariables.getTrueBDD(), globals);
        for (ProductState ps : product.states) {
            allTrans.add(ps, allVals);
        }
        accMasterOptions = computeAccMasterOptions();
        Main.verboseln("Acceptance for master:\n" + this.accMasterOptions);
    }

    protected Map<GSet, Map<Integer, RabinPair>> computeAccSlavesOptions(Formula f) {
        Map<GSet, Map<Integer, RabinPair>> result = new HashMap();
        RabinSlave rSlave = product.slaves.get(f);
        Set<GSet> gSets = powerset(new GSet(topmostGs.get(f)));
        for (GSet gSet : gSets) {
            Map<FormulaState, Boolean> finalStates = new HashMap();
            for (FormulaState fs : rSlave.mojmir.states) {
                finalStates.put(fs, gSet.entails(fs.formula));
            }
            result.put(gSet, new HashMap());
            for (int rank = 1; rank <= maxRank.get(f); rank++) {
                result.get(gSet).put(rank, new RabinPair(rSlave, finalStates, rank, product));
            }
        }
        return result;
    }

    protected Set<GSet> powerset(GSet gSet) {
        Set<GSet> result = new HashSet();
        if (gSet.isEmpty()) {
            result.add(new GSet());
        } else {
            Formula curr = gSet.iterator().next();
            gSet.remove(curr);
            for (GSet gs : powerset(gSet)) {
                result.add(gs);
                GSet gs2 = new GSet(gs);
                gs2.add(curr);
                result.add(gs2);
            }
        }
        return result;
    }

    protected Map<GSet, Map<Map<Formula, Integer>, RabinPair>> computeAccMasterOptions() {
        Map<GSet, Map<Map<Formula, Integer>, RabinPair>> result = new HashMap();
        GSet allGs = new GSet(formula.gSubformulas());
        Set<GSet> gSets = powerset(new GSet(allGs));
        for (GSet gSet : gSets) {
            Main.verboseln("\tGSet " + gSet);
            GSet gSetComplement = new GSet(allGs);
            gSetComplement.removeAll(gSet);
            Set<Map<Formula, Integer>> rankings = powersetRanks(new GSet(gSet));
            for (Map<Formula, Integer> ranking : rankings) {
                Main.verboseln("\t  Ranking " + ranking);
                TranSet<ProductState> avoidP = new TranSet();
                for (ProductState ps : product.states) {
                    /*
                     for (Valuation v : AllValuations.allValuations) { // TODO !!! expl vs bdd
                     if (!slavesEntail(gSet, gSetComplement, ps, ranking, v, ps.masterState.formula)) {
                     avoidP.add(ps, new ValuationSetExplicit(v));
                     }
                     }
                     */
                    avoidP.addAll(computeAccMasterForState(gSet, gSetComplement, ranking, ps));
                }
                if (avoidP.equals(allTrans)) {
                    Main.verboseln("Skipping complete Avoid");
                } else {
                    if (!result.containsKey(gSet)) {
                        result.put(gSet, new HashMap());
                    }
                    if (!result.get(gSet).containsKey(ranking)) {
                        result.get(gSet).put(ranking, new RabinPair(avoidP, null));
                    }
                    Main.verboseln("Avoid for " + gSet + ranking + "\n" + avoidP);
                }
            }
        }
        return result;
    }

    protected Set<Map<Formula, Integer>> powersetRanks(Set<Formula> gSet) {
        Set<Map<Formula, Integer>> result = new HashSet();
        if (gSet.isEmpty()) {
            result.add(new HashMap());
        } else {
            Formula curr = gSet.iterator().next();
            gSet.remove(curr);
            for (Map<Formula, Integer> ranking : powersetRanks(gSet)) {
                for (int rank = 1; rank <= maxRank.get(curr); rank++) {
                    Map<Formula, Integer> rankingNew = new HashMap(ranking);
                    rankingNew.put(curr, rank);
                    result.add(rankingNew);
                }
            }
        }
        return result;
    }

    // symbolic version
    protected TranSet<ProductState> computeAccMasterForState(GSet gSet, GSet gSetComplement, Map<Formula, Integer> ranking, ProductState ps) {
        TranSet<ProductState> result = new TranSet();
        Set<ValuationSet> fineSuccVs = product.generateSuccTransitionsReflectingSinks(ps);
        for (ValuationSet vs : fineSuccVs) {
            if (!slavesEntail(gSet, gSetComplement, ps, ranking, vs.pickAny(), ps.masterState.formula)) {
                result.add(ps, vs);
            }
        }
        return result;
    }

    // unused: explicit version (simpler, likely slower)
    protected TranSet<ProductState> computeAccMasterForState2(GSet gSet, GSet gSetComplement, Map<Formula, Integer> ranking, ProductState ps) {
        TranSet<ProductState> result = new TranSet();
        for (Valuation v : globals.aV.allValuations) { // TODO !!! expl vs bdd
            if (!slavesEntail(gSet, gSetComplement, ps, ranking, v, ps.masterState.formula)) {
                result.add(ps, new ValuationSetExplicit(v, globals));
            }
        }
        return result;
    }

    protected static boolean slavesEntail(GSet gSet, GSet gSetComplement, ProductState ps, Map<Formula, Integer> ranking, Valuation v, Formula consequent) {
        Globals g = consequent.globals;
        Formula antecedent = new BooleanConstant(true, g);
        for (Formula f : gSet) {
            antecedent = new Conjunction(antecedent, new GOperator(f)); // TODO compute these lines once for all states
            antecedent = new Conjunction(antecedent, f.substituteGsToFalse(gSetComplement));
            Formula slaveAntecedent = new BooleanConstant(true, g);
            if (ps.containsKey(f)) {
                for (FormulaState s : ps.get(f).keySet()) {
                    if (ps.get(f).get(s) >= ranking.get(f)) {
                        slaveAntecedent = new Conjunction(slaveAntecedent, s.formula);
                    }
                }
            }
            slaveAntecedent = slaveAntecedent.temporalStep(v).substituteGsToFalse(gSetComplement);
            antecedent = new Conjunction(antecedent, slaveAntecedent);
        }
        return entails(antecedent, consequent.temporalStep(v));
    }

    public static boolean entails(Formula antecedent, Formula consequent) {
        return antecedent.bdd().imp(consequent.bdd()).equals(antecedent.globals.bddForFormulae.trueFormulaBDD());
    }
}
