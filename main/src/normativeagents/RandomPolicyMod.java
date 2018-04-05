/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents;

import burlap.behavior.policy.RandomPolicy;
import burlap.behavior.policy.support.PolicyUndefinedException;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;

import java.util.List;

/**
 *
 * @author dkasenberg
 */
public class RandomPolicyMod extends RandomPolicy {

    public RandomPolicyMod(SADomain domain) {
        super(domain);
    }
    
	@Override
	public Action action(State s) {
		List<Action> gas = Helper.getAllActions(this.actionTypes, s);
		if(gas.size() == 0){
			throw new PolicyUndefinedException();
		}
		Action selection = gas.get(this.rand.nextInt(gas.size()));
		return selection;
	}
    
}
