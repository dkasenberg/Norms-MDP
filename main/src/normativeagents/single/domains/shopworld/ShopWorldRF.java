/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.single.domains.shopworld;

import burlap.mdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.GoalBasedRF;
import normativeagents.single.domains.shopworld.state.ShopWorldAgent;
import normativeagents.single.domains.shopworld.state.ShopWorldTrinket;

import static normativeagents.single.domains.shopworld.ShopWorldDomain.CLASS_AGENT;
import static normativeagents.single.domains.shopworld.ShopWorldDomain.CLASS_TRINKET;

/**
 *
 * @author dkasenberg
 */
public class ShopWorldRF extends GoalBasedRF {
    protected double theftPenalty;

    public ShopWorldRF(TerminalFunction tf, double theftPenalty, double defaultReward) {
        super(new TFGoalCondition(tf));
        this.defaultReward = defaultReward;
        this.theftPenalty = theftPenalty;
    }
        
	@Override
	public double reward(State s, Action a, State sprime) {
		if(gc.satisfies(sprime)){
		    ShopWorldAgent agent = (ShopWorldAgent)((OOState)sprime).object(CLASS_AGENT);
            double rwd = agent.money;
            if(agent.caughtStealing) {
                return rwd + theftPenalty;
            }
            for(ObjectInstance trinket : ((OOState)sprime).objectsOfClass(CLASS_TRINKET)) {
                ShopWorldTrinket t = (ShopWorldTrinket)trinket;
                if(t.held || t.bought) {
                    rwd += t.value;
                }
            }
            return rwd;
		}
		
		return defaultReward;
	}
}
