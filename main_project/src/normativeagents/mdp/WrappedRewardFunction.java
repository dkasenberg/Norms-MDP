package normativeagents.mdp;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import normativeagents.mdp.state.WrapperState;

/**
 * Created by dkasenberg on 10/27/17.
 */
public class WrappedRewardFunction implements RewardFunction {

    public RewardFunction unwrappedRF;

    public WrappedRewardFunction(RewardFunction unwrappedRF) {
        this.unwrappedRF = unwrappedRF;
    }

    @Override
    public double reward(State s, Action a, State sp) {
        try {
            return unwrappedRF.reward(s,a,sp);
        } catch(Exception e) {
            if(s instanceof WrapperState && sp instanceof WrapperState) {
                return reward(((WrapperState) s).s, a, ((WrapperState) sp).s);
            } else {
                return 0;
            }
        }
    }
}
