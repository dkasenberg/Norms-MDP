package normativeagents.rewardvector;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import org.apache.commons.math3.linear.RealVector;

/**
 * An interface for MDP solvers that can return/compute Q-values.
 * @author James MacGlashan
 *
 */
public interface QVectorFunction extends VectorValueFunction{

	/**
	 * Returns the {@link burlap.behavior.valuefunction.QValue} for the given state-action pair.
	 * @param s the input state
	 * @param a the input action
	 * @return the {@link burlap.behavior.valuefunction.QValue} for the given state-action pair.
	 */
    RealVector qValue(State s, Action a);
        
	int size();
}

