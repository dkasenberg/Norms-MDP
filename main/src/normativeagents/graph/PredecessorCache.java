package normativeagents.graph;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import normativeagents.Helper;
import normativeagents.misc.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A cache mapping each state s' to the set of (s,a) pairs such that P(s,a,s') > 0.
 * @author dkasenberg
 */
public class PredecessorCache {
    public Map<HashableState, Set<Pair<HashableState, Action>>> preds;
    public HashableStateFactory hf;

    public PredecessorCache(HashableStateFactory hf) {
        preds = new HashMap<>();
        this.hf = hf;
    }

    /**
     * Determine which (s,a) pairs could precede a given state s.
     * @param s A state
     * @param d The MDP domain.
     * @param allStates the state space of the MDP.
     * @return The set of plausible (state,action) predecessors of s.
     * */
    public Set<Pair<HashableState, Action>> pre(HashableState s, SADomain d, Set<HashableState> allStates) {
        if(!preds.containsKey(s)) return new HashSet<>();
        return preds.get(s).stream().filter(p -> allStates.contains(p.getLeft())).collect(Collectors.toSet());
    }

    /**
     * Compute all of the predecessors on the given domain.
     * */
    public void computePre(SADomain d, Set<HashableState> hs) {
        FullStateModel fsm = (FullStateModel) ((FactoredModel) d.getModel()).getStateModel();
        hs.stream().forEach((preState) -> {
            List<Action> gas = Helper.getAllActions(d, preState.s());
            gas.stream().forEach((ga) -> {
                List<StateTransitionProb> tps = fsm.stateTransitions(preState.s(), ga);
                tps.stream().filter((tp) -> (tp.p > 0)).forEach((tp) -> {
                    HashableState postState = hf.hashState(tp.s);
                    if (!preds.containsKey(postState)) {
                        preds.put(postState, new HashSet<>());
                    }
                    preds.get(postState).add(new Pair(preState, ga));
                });
            });
        });
    }
}
