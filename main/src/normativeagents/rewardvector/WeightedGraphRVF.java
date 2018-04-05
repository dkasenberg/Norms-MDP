/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.rewardvector;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import normativeagents.actions.CRDRAAction;
import normativeagents.misc.Pair;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.Map;

import static normativeagents.actions.CRDRAAction.BREAKONCE;

/**
 *
 * @author dkasenberg
 */
public class WeightedGraphRVF implements RewardVectorFunction {

    public Map<Pair<HashableState, HashableState>, RealVector> weightedGraph;
    protected boolean maximize;
    protected HashableStateFactory hf;
    public RealVector addToAll;
    
    public WeightedGraphRVF(Map<Pair<HashableState,HashableState>,RealVector> weightedGraph, HashableStateFactory hashingFactory) {
        this(weightedGraph, false, hashingFactory);
    }
    
    public WeightedGraphRVF(Map<Pair<HashableState,HashableState>,RealVector> weightedGraph, boolean maximize, HashableStateFactory hashingFactory) {
        this.weightedGraph = weightedGraph;
        this.hf = hashingFactory;
        this.maximize = maximize;
        int n = weightedGraph.values().stream().findAny().get().getDimension();
        this.addToAll = new ArrayRealVector(n);

        if(maximize) {
            for(int i = 0; i < n; i++) {
                final int j = i;
                addToAll.setEntry(i,
                        -1*weightedGraph.values().stream().mapToDouble(l ->
                                l.getEntry(j)).min().orElse(0) + 0.01);
            }
        } else {
            int length = weightedGraph.values().stream().findAny().get().getDimension();
            for(int i = 0; i < length; i++) {
                final int j = i;
                addToAll.setEntry(i,
                        weightedGraph.values().stream().mapToDouble(l ->
                                l.getEntry(j)).max().orElse(0) + 0.01);
            }
        }

//        this.addToAll= 0;
    }
    
    @Override
    public RealVector rv(State s, Action ga, State sp) {
        double multiplier = maximize ? 1.0 : -1.0;
        RealVector rewardVector = addToAll.copy();
        for(int i =0; i < addToAll.getDimension(); i++) {
            if(ga instanceof CRDRAAction && ((CRDRAAction) ga).settings[i] == BREAKONCE) {
                rewardVector.addToEntry(i,multiplier);
            }
        }
        return rewardVector;
//        Pair<HashableState, HashableState> p = new Pair(hf.hashState(s), hf.hashState(sp));
//        try {
//            RealVector weight = weightedGraph.get(p);
//            return weight.mapMultiply(multiplier).add(addToAll);
//        } catch(Exception e) {
//            return addToAll;
//        }
    }
    
}
