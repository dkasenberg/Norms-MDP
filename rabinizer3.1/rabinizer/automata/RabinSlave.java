/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

import java.util.*;
import rabinizer.bdd.*;
import rabinizer.exec.Main;
import rabinizer.deleteOld.Misc;
import rabinizer.exec.Tuple;

/**
 *
 * @author jkretinsky
 */
public class RabinSlave extends Automaton<RankingState> {

    public FormulaAutomaton mojmir;

    public RabinSlave(FormulaAutomaton mojmir) {
        super(mojmir.globals);
        this.mojmir = mojmir;
    }

    @Override
    protected RankingState generateInitialState() {
        RankingState init = new RankingState();
        init.put(mojmir.initialState, 1);
        return init;
    }

    @Override
    protected RankingState generateSuccState(RankingState curr, ValuationSet vs) {
        Valuation val = vs.pickAny();
        RankingState succ = new RankingState();

        // move tokens, keeping the lowest only
        for (FormulaState currFormula : curr.keySet()) {
            FormulaState succFormula = mojmir.succ(currFormula, val);
            if ((succ.get(succFormula) == null) || (succ.get(succFormula) > curr.get(currFormula))) {
                succ.put(succFormula, curr.get(currFormula));
            }
        }
        for (FormulaState s : mojmir.sinks) {
            succ.remove(s);
        }

        //TODO recompute tokens, eliminating gaps
        int[] tokens = new int[succ.keySet().size()];
        int i = 0;
        for (FormulaState f : succ.keySet()) {
            tokens[i] = succ.get(f);
            i++;
        }
        Arrays.sort(tokens);
        for (FormulaState f : succ.keySet()) {
            for (int j = 0; j < tokens.length; j++) {
                if (succ.get(f).equals(tokens[j])) {
                    succ.put(f, j + 1);
                }
            }
        }

        //TODO add token to the initial state
        if (!succ.containsKey(mojmir.initialState)) {
            succ.put(mojmir.initialState, succ.keySet().size() + 1);
        }

        return succ;
    }

    @Override
    protected Set<ValuationSet> generateSuccTransitions(RankingState s) {
        Set<Set<ValuationSet>> product = new HashSet();
        for (FormulaState fs : s.keySet()) {
            product.add(mojmir.transitions.get(fs).keySet());
        }
        return generatePartitioning(product);
    }

    public RabinSlave optimizeInitialState() { // TODO better: reach BSCC
        while (noIncomingTransitions(initialState) && !transitions.get(initialState).isEmpty()) {
            Main.verboseln("Optimizing initial states");
            RankingState oldInit = initialState;
            initialState = succ(initialState, new Valuation(globals.bddForVariables.numOfVariables, globals));
            transitions.remove(oldInit);
            states.remove(oldInit);
        }
        return this;
    }

    private boolean noIncomingTransitions(RankingState in) {
        for (RankingState out : states) {
            if (edgeBetween.containsKey(new Tuple(out, in))) {
                return false;
            }
        }
        return true;
    }

}
