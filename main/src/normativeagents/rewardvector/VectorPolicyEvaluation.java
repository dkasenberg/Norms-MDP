package normativeagents.rewardvector;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import normativeagents.actions.CRDRAActionType;
import normativeagents.mdp.state.CRDRAProductState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.linear.RealVector;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * This class is used to compute the value function under some specified policy. The value function is computed using tabular
 * Value Iteration with the Bellman operator being fixed to the specified policy. After constructing an instance
 * use the {@link #evaluatePolicy(EnumerablePolicy, State)} method to evaluate a
 * policy from some initial seed state. You can reuse this class to evaluate different subsequent policies, but doing so
 * will overwrite the value function. If you want to save the value function that was computed for some policy,
 * use the {@link #getCopyOfValueFunction()} method.
 * <p>
 * Alternatively, you can also evaluate a policy with the {@link #evaluatePolicy(EnumerablePolicy)} method,
 * but you should have already seeded the state space by having called the {@link #evaluatePolicy(EnumerablePolicy, State)}
 * method or the {@link #performReachabilityFrom(State)} method at least once previously,
 * a runtime exception will be thrown.
 *
 * @author James MacGlashan.
 */
public class VectorPolicyEvaluation extends RVValueIteration {

    /**
     * When the maximum change in the value function is smaller than this value, policy evaluation will terminate.
     */
    protected double maxEvalDelta;


    /**
     * When the maximum number of evaluation iterations passes this number, policy evaluation will terminate
     */
    protected double maxEvalIterations;

    protected Log log = LogFactory.getLog(VectorPolicyEvaluation.class);

    /**
     * Initializes.
     * @param domain the domain on which to evaluate a policy
     * @param gamma the discount factor
     * @param hashingFactory the {@link burlap.statehashing.HashableStateFactory} used to index states and perform state equality
     * @param maxEvalDelta the minimum change in the value function that will cause policy evaluation to terminate
     * @param maxEvalIterations the maximum number of evaluation iterations to perform before terminating policy evaluation
     */
    public VectorPolicyEvaluation(SADomain domain, double gamma, HashableStateFactory hashingFactory,
                                  double maxEvalDelta, int maxEvalIterations, Set<HashableState> noUpdate,
                                  RewardVectorFunction rvf, int size, Comparator comp, boolean ignoreTF) {
        super(domain, gamma, hashingFactory, maxEvalDelta, maxEvalIterations, rvf, size, comp,
                ignoreTF, noUpdate);
        this.maxEvalDelta = maxEvalDelta;
        this.maxEvalIterations = maxEvalIterations;
        Logger.getLogger(VectorPolicyEvaluation.class).setLevel(Level.WARN);
    }

    /**
     * Initializes.
     * @param domain the domain on which to evaluate a policy
     * @param gamma the discount factor
     * @param hashingFactory the {@link burlap.statehashing.HashableStateFactory} used to index states and perform state equality
     * @param maxEvalDelta the minimum change in the value function that will cause policy evaluation to terminate
     * @param maxEvalIterations the maximum number of evaluation iterations to perform before terminating policy evaluation
     */
    public VectorPolicyEvaluation(SADomain domain, double gamma, HashableStateFactory hashingFactory,
                                  double maxEvalDelta, int maxEvalIterations, Set<HashableState> noUpdate,
                                  RewardVectorFunction rvf, int size, Comparator comp) {
        this(domain, gamma, hashingFactory, maxEvalDelta, maxEvalIterations, noUpdate, rvf, size, comp, true);
    }


    /**
     * Computes the value function for the given policy after finding all reachable states from seed state s
     * @param policy The {@link burlap.behavior.policy.Policy} to evaluate
     * @param s the seed initiate state from which to find all reachable states
     */
    public void evaluatePolicy(EnumerablePolicy policy, State s){
        this.performReachabilityFrom(s);
        this.evaluatePolicy(policy);
    }


    /**
     * Computes the value function for the given policy over the states that have been discovered
     * @param policy the {@link burlap.behavior.policy.Policy} to evaluate
     */
    public void evaluatePolicy(EnumerablePolicy policy){

        if(this.valueFunction.size() == 0){
            throw new RuntimeException("Cannot evaluate policy, because no states have been expanded. Use the performStateReachability method" +
                    "or call the evaluatePolicy method that takes a seed initial state as input.");
        }


        double maxChangeInPolicyEvaluation = Double.NEGATIVE_INFINITY;

        Set <HashableState> states = valueFunction.keySet();

        log.info("STATES: " + states.size());
        states.removeAll(noUpdate);
        log.info("NOUPDATE: " + noUpdate.size());

        int i;
        for(i = 0; i < this.maxEvalIterations; i++){

            double delta = 0.;
            for(HashableState sh : states){
//                System.out.println(policy.policyDistribution(sh.s()));
                RealVector v = this.value(sh);
                RealVector maxQ = this.performFixedPolicyBellmanUpdateOn(sh, policy);
//                System.out.println(sh.s());
//                System.out.println(maxQ);
                double distance = v.getDistance(maxQ);
                delta = Math.max(distance, delta);

            }

            maxChangeInPolicyEvaluation = Math.max(delta, maxChangeInPolicyEvaluation);

            log.info("Finished iteration " + i +", delta: " + delta);

            if(delta < this.maxEvalDelta){
                i++;
                break; //approximated well enough; stop iterating
            }

        }


    }

    @Override
    protected List<Action> applicableActions(State s) {
        if(!(s instanceof CRDRAProductState)) throw new RuntimeException("Weight optimization only works with CRDRA product");
        List<Action> retVal = new ArrayList<>();
        for(ActionType at : domain.getActionTypes()) {
            if(at instanceof CRDRAActionType) {
                retVal.addAll(((CRDRAActionType)at).allApplicableNonProductActions(s));
            } else {
                retVal.addAll(at.allApplicableActions(s));
            }
        }
        return retVal;
    }
}
