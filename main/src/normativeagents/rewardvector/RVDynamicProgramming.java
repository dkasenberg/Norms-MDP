package normativeagents.rewardvector;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.planning.stochastic.dpoperator.BellmanOperator;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import normativeagents.actions.CRDRAAction;
import normativeagents.actions.CRDRAActionType;
import normativeagents.mdp.model.CRDRAProductModel;
import normativeagents.mdp.state.CRDRAProductState;
import normativeagents.misc.Pair;
import normativeagents.misc.Triple;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

/**
 * A class for performing dynamic programming operations: updating the value function using a Bellman backup.
 * @author dkasenberg
 *
 */
public class RVDynamicProgramming extends MDPSolver implements VectorValueFunction, QVectorProvider {



	/**
	 * A map for storing the current value function estimate for each state.
	 */
	protected Map <HashableState, RealVector>							valueFunction;


	/**
	 * The value function initialization to use; defaulted to an initialization of 0 everywhere.
	 */
	protected VectorValueFunction valueInitializer;


	protected VectorDPOperator operator = new VectorBellmanOperator();
	protected Comparator<RealVector> vectorCompare;
	protected int size;

	protected RewardVectorFunction rvf;

//	If this is turned on, ignore the terminal function (treating the MDP as if it lasts forever)
	protected boolean ignoreTF;

	public Set<int[]> possibleSettings;
	protected Set<HashableState> noUpdate;


	/**
	 * Common init method for {@link burlap.behavior.singleagent.planning.stochastic.DynamicProgramming} instances. This will automatically call the
	 * {@link burlap.behavior.singleagent.MDPSolver#solverInit(SADomain, double, HashableStateFactory)}
	 * method.
	 * @param domain the domain in which to plan
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory
	 */
	public void DPPInit(SADomain domain, double gamma, HashableStateFactory hashingFactory, RewardVectorFunction rvf, int vectorSize, Comparator vectorCompare, boolean ignoreTF, Set<HashableState> noUpdate){
		this.valueInitializer = new ConstantVectorValueFunction(vectorSize);

		this.solverInit(domain, gamma, hashingFactory);
		this.noUpdate = noUpdate;


		this.valueFunction = new HashMap<HashableState, RealVector>();

		this.size = vectorSize;
		this.possibleSettings = CRDRAActionType.getPossibleSettings(size);

		this.vectorCompare = vectorCompare;

		this.rvf = rvf;
		this.ignoreTF = ignoreTF;

	}

	public Map<HashableState, RealVector> getValueFunctionMap() {
		return new HashMap<>(this.valueFunction);
	}

//	This lets us save and load the value functions.
	public void setValueFunctionMap(Map<HashableState,RealVector> valueFunction) {
		this.valueFunction = new HashMap<>(valueFunction);
	}

	@Override
	public SampleModel getModel() {
		return this.model;
	}

	@Override
	public void resetSolver(){
		this.valueFunction.clear();
	}

	/**
	 * Sets the value function initialization to use.
	 * @param vfInit the object that defines how to initializes the value function.
	 */
	public void setValueFunctionInitialization(VectorValueFunction vfInit){
		this.valueInitializer = vfInit;
	}

	/**
	 * Returns the value initialization function used.
	 * @return the value initialization function used.
	 */
	public VectorValueFunction getValueFunctionInitialization(){
		return this.valueInitializer;
	}


	/**
	 * Returns the dynamic programming operator used
	 * @return the dynamic programming operator used
	 */
	public VectorDPOperator getOperator() {
		return operator;
	}

	/**
	 * Sets the dynamic programming operator use. Note that default setting is {@link BellmanOperator} (max)
	 * @param operator the dynamic programming operator to use.
	 */
	public void setOperator(VectorDPOperator operator) {
		this.operator = operator;
	}

	/**
	 * Returns whether a value for the given state has been computed previously.
	 * @param s the state to check
	 * @return true if the the value for the given state has already been computed; false otherwise.
	 */
	public boolean hasComputedValueFor(State s){
		HashableState sh = this.hashingFactory.hashState(s);
		return this.valueFunction.containsKey(sh);
	}


	/**
	 * Returns the value function evaluation of the given state. If the value is not stored, then the default value
	 * specified by the ValueFunctionInitialization object of this class is returned.
	 * @param s the state to evaluate.
	 * @return the value function evaluation of the given state.
	 */
	@Override
	public RealVector value(State s){
		if(!ignoreTF && this.model.terminal(s)){
			return new OpenMapRealVector(size);
		}
		HashableState sh = this.hashingFactory.hashState(s);
		return this.value(sh);
	}

	/**
	 * Returns the value function evaluation of the given hashed state. If the value is not stored, then the default value
	 * specified by the ValueFunctionInitialization object of this class is returned.
	 * @param sh the hashed state to evaluate.
	 * @return the value function evaluation of the given state.
	 */
	public RealVector value(HashableState sh){
		if(!ignoreTF && this.model.terminal(sh.s())){
			return new OpenMapRealVector(size);
		}
		RealVector V = valueFunction.get(sh);
		RealVector v = V == null ? this.getDefaultValue(sh.s()) : V;
		return v;
	}




	@Override
	public List <QValueVector> qValues(State s){

		List<Action> gas = this.applicableActions(s);
		List<QValueVector> qs = new ArrayList<QValueVector>(gas.size());
		for(Action ga : gas){
			QValueVector q = new QValueVector(s, ga, this.qValue(s, ga));
			qs.add(q);
		}

		return qs;

	}



	@Override
	public RealVector qValue(State s, Action a){

		RealVector dq = this.computeQ(s, a);
		return dq;

	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Comparator getComparator() {
		return vectorCompare;
	}


	/**
	 * This method will return all states that are stored in this planners value function.
	 * @return all states that are stored in this planners value function.
	 */
	public List <State> getAllStates(){
		List <State> result = new ArrayList<State>(valueFunction.size());
		Set<HashableState> shs = valueFunction.keySet();
		for(HashableState sh : shs){
			result.add(sh.s());
		}
		return result;
	}


	public RVDynamicProgramming getCopyOfValueFunction(){

		RVDynamicProgramming dpCopy = new RVDynamicProgramming();
		dpCopy.DPPInit(this.domain, this.gamma, this.hashingFactory, this.rvf, this.size, this.vectorCompare, this.ignoreTF, this.noUpdate);


		//copy the value function
		for(Map.Entry<HashableState, RealVector> e : this.valueFunction.entrySet()){
			dpCopy.valueFunction.put(e.getKey(), e.getValue());
		}
		return dpCopy;
	}









	/**
	 * Performs a Bellman value function update on the provided state. Results are stored in the value function map as well as returned.
	 * If this object is set to used cached transition dynamics and the transition dynamics for this state are not cached, then they will be created and cached.
	 * @param s the state on which to perform the Bellman update.
	 * @return the new value of the state.
	 */
	public RealVector performBellmanUpdateOn(State s){
		return this.performBellmanUpdateOn(this.stateHash(s));
	}


	/**
	 * Performs a fixed-policy Bellman value function update (i.e., policy evaluation) on the provided state. Results are stored in the value function map as well as returned.
	 * If this object is set to used cached transition dynamics and the transition dynamics for this state are not cached, then they will be created and cached.
	 * @param s the state on which to perform the Bellman update.
	 * @param p the policy that is being evaluated
	 * @return the new value of the state
	 */
	public RealVector performFixedPolicyBellmanUpdateOn(State s, EnumerablePolicy p){
		return this.performFixedPolicyBellmanUpdateOn(this.stateHash(s), p);
	}

	/**
	 * Writes the value function table stored in this object to the specified file path.
	 * Uses a standard YAML approach, which means the HashableState and underlying Domain states
	 * must have JavaBean like properties; i.e., have a default constructor and getters and setters (or public data
	 * members) for all relevant fields.
	 * @param path the path to write the value function
	 */
	public void writeValueTable(String path){
		Yaml yaml = new Yaml();
		try {
			yaml.dump(this.valueFunction, new BufferedWriter(new FileWriter(path)));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the value function table located on disk at the specified path. Expects the file to be a Yaml
	 * representation of a Java {@link Map} from {@link HashableState} to {@link Double}.
	 * @param path the path to the save value function table
	 */
	public void loadValueTable(String path){
		Yaml yaml = new Yaml();
		try {
			this.valueFunction = (Map<HashableState, RealVector>)yaml.load(new FileReader(path));
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Performs a Bellman value function update on the provided (hashed) state. Results are stored in the value function map as well as returned.
	 * If this object is set to used cached transition dynamics and the transition dynamics for this state are not cached, then they will be created and cached.
	 * @param sh the hashed state on which to perform the Bellman update.
	 * @return the new value of the state.
	 */
	protected RealVector  performBellmanUpdateOn(HashableState sh){

		if(!ignoreTF && model.terminal(sh.s())){
			//terminal states always have a state value of 0
			RealVector retVal = new OpenMapRealVector(size);
			valueFunction.put(sh, retVal);
			return retVal;
		}


		List<Action> gas = this.applicableActions(sh.s());

		RealVector [] qs = new RealVector[gas.size()];

		int i = 0;
		for(Action ga : gas){
			RealVector q = this.computeQ(sh.s(), ga);
			qs[i] = q;
			i++;
		}

		RealVector nv = operator.apply(qs, vectorCompare);
		valueFunction.put(sh, nv);

		return nv;
	}




	/**
	 * Performs a fixed-policy Bellman value function update (i.e., policy evaluation) on the provided state. Results are stored in the value function map as well as returned.
	 * @param sh the hashed state on which to perform the Bellman update.
	 * @param p the policy that is being evaluated
	 * @return the new value of the state
	 */
	protected RealVector performFixedPolicyBellmanUpdateOn(HashableState sh, EnumerablePolicy p){

		if(!ignoreTF && this.model.terminal(sh.s())){
			//terminal states always have a state value of 0
			RealVector retVal = new OpenMapRealVector(size);
			valueFunction.put(sh, retVal);
			return retVal;
		}

		RealVector weightedQ = new ArrayRealVector(size);
		List<ActionProb> policyDistribution = p.policyDistribution(sh.s());


		//List <GroundedAction> gas = sh.s.getAllGroundedActionsFor(this.actions);
		List<Action> gas = this.applicableActions(sh.s());
		for(Action ga : gas){

			double policyProb = PolicyUtils.actionProbGivenDistribution(ga, policyDistribution);

			if(policyProb == 0.){
				continue; //doesn't contribute
			}


			RealVector q = this.computeQ(sh.s(), ga);
			weightedQ = weightedQ.add(q.mapMultiply(policyProb));
		}

		valueFunction.put(sh, weightedQ);

		return weightedQ;

	}




	/**
	 * Computes the Q-value This computation
	 * *is* compatible with {@link burlap.behavior.singleagent.options.Option} objects.
	 * @param s the given state
	 * @param ga the given action
	 * @return the double value of a Q-value for the given state-aciton pair.
	 */
	protected RealVector computeQ(State s, Action ga){

		RealVector q = new ArrayRealVector(size);

		if(ga instanceof CRDRAAction) ga = ((CRDRAAction) ga).action;

		List<Pair<Double, RealVector>> outcomes = new ArrayList();
		List<TransitionProb> tps = ((FullModel)model).transitions(s, new CRDRAAction(ga,size));

		for(TransitionProb tp : tps){
			State sp = ((CRDRAProductState)tp.eo.op).s;
			RealVector valSp = getBestStateTransition(s, ga, sp).getRight();
			q = q.add(valSp.mapMultiply(tp.p));
//				q += tp.p * (r + (discount * vp));
		}


		return q;
	}

	public Triple<CRDRAAction,CRDRAProductState,RealVector> getBestStateTransition(State s, Action a, State sp) {
		return possibleSettings.stream().map((settings) -> {
			CRDRAAction newGA = new CRDRAAction(a.copy());
			newGA.setSettings(settings);
			return newGA;
		}).map((newGA) -> {
			CRDRAProductState newSp = (CRDRAProductState)((CRDRAProductModel) ((FactoredModel) model).getStateModel()).getProductTransition((CRDRAProductState) s, newGA, sp);
			RealVector value = this.rvf.rv(s,newGA,newSp).add(this.value(newSp).mapMultiply(this.gamma));
			return new Triple<>(newGA, newSp, value);
		}).filter(t->noUpdate.contains(hashingFactory.hashState(s)) || !noUpdate.contains(hashingFactory.hashState(t.getMiddle()))).max(Comparator.comparing(p -> p.getRight(),
				vectorCompare)).orElseThrow(()->new RuntimeException("Empty"));
	}



	/**
	 * Returns the default V-value to use for the state
	 * @param s the input state to get the default V-value for
	 * @return the default V-value in double form.
	 */
	protected RealVector getDefaultValue(State s){
		return this.valueInitializer.value(s);
	}


}
