package normativeagents.rewardvector;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import org.apache.commons.math3.linear.RealVector;


/**
 * This class is used to store Q-values.
 * @author James MacGlashan
 *
 */
public class QValueVector {
	
	/**
	 * The state with which this Q-value is associated.
	 */
	public State s;
	
	/**
	 * The action with which this Q-value is associated
	 */
	public Action a;
	
	/**
	 * The numeric Q-value
	 */
	public RealVector q;
	
	
	
	/**
	 * Creates a Q-value for the given state an action pair with the specified q-value
	 * @param s the state
	 * @param a the action
	 * @param q the initial Q-value
	 */
	public QValueVector(State s, Action a, RealVector q){
		this.s = s;
		this.a = a;
		this.q = q;
	}
	
	
	/**
	 * Initializes this Q-value by copying the information from another Q-value.
	 * @param src the source Q-value from which to copy.
	 */
	public QValueVector(QValueVector src){
		this.s = src.s.copy();
		this.a = src.a.copy();
		this.q = src.q.copy();
	}

	@Override
	public String toString() {
		return a.toString() + ": " + q.toString();
	}
}
