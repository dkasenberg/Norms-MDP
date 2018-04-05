package normativeagents.rewardvector;

import burlap.behavior.singleagent.planning.Planner;
import burlap.debugtools.DPrint;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import org.apache.commons.math3.linear.RealVector;

import java.util.*;


/**
 * An implementation of asynchronous value iteration. Values of states are updated using the Bellman operator in an arbitrary order and a complete pass
 * over the state space is performed on each iteration. VI can be set to terminate under two possible conditions: when the maximum change in the value
 * function is smaller than some threshold or when a threshold of iterations is passed. This implementation first determines the state space by finding
 * all reachable states from a source state. The worst case time complexity of the reachability operation is equivalent to that of one VI iteration and has the added benefit
 * that VI does not pass over non-reachable states.
 *
 * This implementation is compatible with options.
 *
 *
 * @author dkasenberg
 *
 */
public class RVValueIteration extends RVDynamicProgramming implements Planner {

    /**
     * When the maximum change in the value function is smaller than this value, VI will terminate.
     */
    protected double												maxDelta;

    /**
     * When the number of VI iterations exceeds this value, VI will terminate.
     */
    protected int													maxIterations;


    /**
     * Indicates whether the reachable states has been computed yet.
     */
    protected boolean												foundReachableStates = false;


    /**
     * When the reachability analysis to find the state space is performed, a breadth first search-like pass
     * (spreading over all stochastic transitions) is performed. It can optionally be set so that the
     * search is pruned at terminal states by setting this value to true. By default, it is false and the full
     * reachable state space is found
     */
    protected boolean												stopReachabilityFromTerminalStates = false;


    protected boolean												hasRunVI = false;

    public RVValueIteration(SADomain domain, double gamma, HashableStateFactory hashingFactory, double maxDelta,
                            int maxIterations, RewardVectorFunction rvf, int vectorSize, Comparator vectorCompare){
        this(domain, gamma, hashingFactory, maxDelta, maxIterations, rvf, vectorSize, vectorCompare, true,
                new HashSet<>());

    }

    /**
     * Initializers the valueFunction.
     * @param domain the domain in which to plan
     * @param gamma the discount factor
     * @param hashingFactory the state hashing factor to use
     * @param maxDelta when the maximum change in the value function is smaller than this value, VI will terminate.
     * @param maxIterations when the number of VI iterations exceeds this value, VI will terminate.
     */
    public RVValueIteration(SADomain domain, double gamma, HashableStateFactory hashingFactory, double maxDelta,
                            int maxIterations, RewardVectorFunction rvf, int vectorSize, Comparator vectorCompare,
                            boolean ignoreTF, Set<HashableState> noUpdate){

        this.DPPInit(domain, gamma, hashingFactory, rvf, vectorSize, vectorCompare, ignoreTF, noUpdate);
        this.maxDelta = maxDelta;
        this.maxIterations = maxIterations;

    }


    /**
     * Calling this method will force the valueFunction to recompute the reachable states when the {@link #planFromState(State)} method is called next.
     * This may be useful if the transition dynamics from the last planning call have changed and if planning needs to be restarted as a result.
     */
    public void recomputeReachableStates(){
        this.foundReachableStates = false;
    }


    /**
     * Sets whether the state reachability search to generate the state space will be prune the search from terminal states.
     * The default is not to prune.
     * @param toggle true if the search should prune the search at terminal states; false if the search should find all reachable states regardless of terminal states.
     */
    public void toggleReachabiltiyTerminalStatePruning(boolean toggle){
        this.stopReachabilityFromTerminalStates = toggle;
    }


    /**
     * Plans from the input state and then returns a {@link burlap.behavior.policy.GreedyQPolicy} that greedily
     * selects the action with the highest Q-value and breaks ties uniformly randomly.
     * @param initialState the initial state of the planning problem
     * @return a {@link burlap.behavior.policy.GreedyQPolicy}.
     */
    @Override
    public GreedyQVectorPolicy planFromState(State initialState){
        if(this.performReachabilityFrom(initialState) || !this.hasRunVI){
            this.runVI();
        }

        return new GreedyQVectorPolicy(this);
    }

    @Override
    public void resetSolver(){
        super.resetSolver();
        this.foundReachableStates = false;
        this.hasRunVI = false;
    }

    /**
     * Runs VI until the specified termination conditions are met. In general, this method should only be called indirectly through the {@link #planFromState(State)} method.
     * The {@link #performReachabilityFrom(State)} must have been performed at least once
     * in the past or a runtime exception will be thrown. The {@link #planFromState(State)} method will automatically call the {@link #performReachabilityFrom(State)}
     * method first and then this if it hasn't been run.
     */
    public void runVI(){

        if(!this.foundReachableStates){
            throw new RuntimeException("Cannot run VI until the reachable states have been found. Use the planFromState or performReachabilityFrom method at least once before calling runVI.");
        }

        Set <HashableState> states = valueFunction.keySet();
        states.removeAll(noUpdate);

        int i;
        for(i = 0; i < this.maxIterations; i++){

            double delta = 0.;
            for(HashableState sh : states){

                RealVector v = this.value(sh);
                RealVector maxQ = this.performBellmanUpdateOn(sh);
                double distance = v.getDistance(maxQ);
                delta = Math.max(distance, delta);

            }

            DPrint.cl(this.debugCode, "Finished iteration " + i +", delta: " + delta);
            if(delta < this.maxDelta){
                break; //approximated well enough; stop iterating
            }

        }

        DPrint.cl(this.debugCode, "Passes: " + i);

        this.hasRunVI = true;

    }

    /**
     * This method will find all reachable states that will be used by the {@link #runVI()} method and will cache all the transition dynamics.
     * This method will not do anything if all reachable states from the input state have been discovered from previous calls to this method.
     * @param si the source state from which all reachable states will be found
     * @return true if a reachability analysis had never been performed from this state; false otherwise.
     */
    public boolean performReachabilityFrom(State si){

        HashableState sih = this.stateHash(si);
        //if this is not a new state and we are not required to perform a new reachability analysis, then this method does not need to do anything.
        if(valueFunction.containsKey(sih) && this.foundReachableStates){
            return false; //no need for additional reachability testing
        }

        DPrint.cl(this.debugCode, "Starting reachability analysis");

        //add to the open list
        LinkedList <HashableState> openList = new LinkedList<HashableState>();
        Set <HashableState> openedSet = new HashSet<HashableState>();
        openList.offer(sih);
        openedSet.add(sih);


        while(!openList.isEmpty()){
            HashableState sh = openList.poll();

            //skip this if it's already been expanded
            if(valueFunction.containsKey(sh)){
                continue;
            }

            //do not need to expand from terminal states if set to prune
            if(this.model.terminal(sh.s()) && stopReachabilityFromTerminalStates){
                continue;
            }

            this.valueFunction.put(sh, this.valueInitializer.value(sh.s()));


            List<Action> actions = this.applicableActions(sh.s());
            for(Action a : actions){
                List<TransitionProb> tps = ((FullModel)model).transitions(sh.s(), a);
                for(TransitionProb tp : tps){
                    if(tp.p <= 0.0) continue;
                    HashableState tsh = this.stateHash(tp.eo.op);
                    if(!openedSet.contains(tsh) && !valueFunction.containsKey(tsh)){
                        openedSet.add(tsh);
                        openList.offer(tsh);
                    }
                }
            }


        }

        DPrint.cl(this.debugCode, "Finished reachability analysis; # states: " + valueFunction.size());

        this.foundReachableStates = true;
        this.hasRunVI = false;

        return true;

    }






}
