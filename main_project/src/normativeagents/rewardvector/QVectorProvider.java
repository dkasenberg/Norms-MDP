package normativeagents.rewardvector;


import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.support.ActionProb;
import burlap.mdp.core.state.State;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.Comparator;
import java.util.List;

/**
 * An interface for MDP solvers that can return/compute Q-values. Provides a method for generating the set of relevant Q-values.
 * @author James MacGlashan
 *
 */
public interface QVectorProvider extends QVectorFunction {

    Comparator getComparator();


    /**
     * Returns a {@link java.util.List} of {@link burlap.behavior.valuefunction.QValue} objects for ever permissible action for the given input state.
     * @param s the state for which Q-values are to be returned.
     * @return a {@link java.util.List} of {@link burlap.behavior.valuefunction.QValue} objects for ever permissible action for the given input state.
     */
    List <QValueVector> qValues(State s);

    int size();


    /**
     * A class of helper static methods that may be commonly used by code that uses a QFunction instance. In particular,
     * methods for computing the value function of a state, given the Q-values (the max Q-value or policy weighted value).
     */
    class Helper {

        private Helper() {
            // do nothing
        }

        /**
         * Returns the optimal state value function for a state given a {@link QVectorProvider}.
         * The optimal value is the max Q-value. If no actions are permissible in the input state, then zero is returned.
         * @param qSource the {@link QVectorProvider} capable of producing Q-values.
         * @param s the query {@link State} for which the value should be returned.
         * @return the max Q-value for all possible Q-values in the state.
         */
        public static RealVector maxQ(QVectorProvider qSource, State s){
            List <QValueVector> qs = qSource.qValues(s);
            RealVector maxq = new ArrayRealVector(qSource.size(), Double.NEGATIVE_INFINITY);
            if(qs.isEmpty()){
                return maxq;
            }
            for(QValueVector q : qs){
                if(qSource.getComparator().compare(q.q, maxq) > 0) {
                    maxq = q.q;
                }
            }
            return maxq;
        }



        /**
         * Returns the state value under a given policy for a state and {@link QVectorProvider}.
         * The value is the expected Q-value under the input policy action distribution. If no actions are permissible in the input state, then zero is returned.
         * @param qSource the {@link QVectorProvider} capable of producing Q-values.
         * @param s the query {@link State} for which the value should be returned.
         * @param p the policy defining the action distribution.
         * @return the expected Q-value under the input policy action distribution
         */
        public static RealVector policyValue(QVectorProvider qSource, State s, EnumerablePolicy p){
            RealVector expectedValue = new ArrayRealVector(qSource.size());
            List <ActionProb> aps = p.policyDistribution(s);
            if(aps.isEmpty()){
                return expectedValue;
            }
            for(ActionProb ap : aps){
                RealVector q = qSource.qValue(s, ap.ga);
                expectedValue = expectedValue.add(q.mapMultiply(ap.pSelection));
            }
            return expectedValue;
        }


    }

}

