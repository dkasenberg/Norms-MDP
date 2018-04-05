/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents;

import burlap.behavior.policy.RandomPolicy;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.policy.support.PolicyUndefinedException;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import normativeagents.actions.CRDRAAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author dkasenberg
 */
public class RestrictedRandomPolicy extends RandomPolicy {

    public Map<HashableState, List<Action>> allowedActions;
    public HashableStateFactory hashingFactory;
    
    public RestrictedRandomPolicy(SADomain domain, Map<HashableState, Set<Action>> allowedActions, HashableStateFactory hashingFactory) {
        super(domain);

        this.allowedActions = allowedActions.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> new ArrayList<>(e.getValue())));
        this.hashingFactory = hashingFactory;
    }
    
    @Override
    public Action action(State s) {
        HashableState hs = hashingFactory.hashState(s);
        List<Action> gas;
        if(allowedActions.containsKey(hs)) {
            gas = allowedActions.get(hs);
        } else {
            gas = Helper.getAllActions(this.actionTypes, s).stream().map(a->{
                if(a instanceof CRDRAAction) {
                    return ((CRDRAAction)a).action;
                } else {
                    return a;
                }
            }).collect(Collectors.toList());
        }
            if(gas.isEmpty()){
                    throw new PolicyUndefinedException();
            }
            Action selection = gas.get(this.rand.nextInt(gas.size()));
            return selection;
    }

	@Override
	public List<ActionProb> policyDistribution(State s) {
            HashableState hs = hashingFactory.hashState(s);
            List<Action> gas;
            if(allowedActions.containsKey(hs)) {
                gas = allowedActions.get(hs);
            } else {
                gas = Helper.getAllActions(this.actionTypes, s).stream().map(a->{
                    if(a instanceof CRDRAAction) {
                        return ((CRDRAAction)a).action;
                    } else {
                        return a;
                    }
                }).distinct().collect(Collectors.toList());
            }
            if(gas.isEmpty()){
                    throw new PolicyUndefinedException();
            }
            double p = 1./gas.size();
            List<ActionProb> aps = new ArrayList<>(gas.size());
            gas.stream().map((ga) -> new ActionProb(ga, p)).forEach((ap) -> {
                aps.add(ap);
        });
            return aps;
	}

	@Override
	public boolean definedFor(State s) {
            HashableState hs = hashingFactory.hashState(s);
            List<Action> gas;
            if(allowedActions.containsKey(hs)) {
                gas = allowedActions.get(hs);
            } else {
                gas = Helper.getAllActions(this.actionTypes, s);
            }
            return !gas.isEmpty();
	}
    
}
