/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.rewardvector;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author dkasenberg
 */
public interface RewardVectorFunction {
    RealVector rv(State s, Action ga, State sp);
}
