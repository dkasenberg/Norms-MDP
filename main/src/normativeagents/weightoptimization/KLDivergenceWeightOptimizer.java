package normativeagents.weightoptimization;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.debugtools.DPrint;
import burlap.mdp.auxiliary.common.ConstantStateGenerator;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import normativeagents.RestrictedQ;
import normativeagents.RestrictedRandomPolicy;
import normativeagents.actions.CRDRAAction;
import normativeagents.mdp.state.CRDRAProductState;
import normativeagents.mdp.state.WrapperState;
import normativeagents.misc.DefaultHashMap;
import normativeagents.normconflictresolution.NormConflictResolver;
import normativeagents.rewardvector.RewardVectorFunction;
import normativeagents.rewardvector.VectorPolicyEvaluation;
import normativeagents.rewardvector.WeightedGraphRVF;
import normativeagents.rewardvector.comparator.WeightedSumComparator;
import normativeagents.single.domains.shocks.ShockDomain;
import normativeagents.single.domains.shocks.ShockWorldVictim;
import normativeagents.statehashing.HashableWrapperStateFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by dkasenberg on 9/26/17.
 */
public class KLDivergenceWeightOptimizer extends VectorPolicyEvaluation {

    public RealVector currentWeights;
    public Map<HashableState, RealVector> weightGradients;
    public Map<HashableState, Double> objective;
    public Map<HashableState,Set<Action>> correctPolicy;
    public State initialState;
    public double learningRate;
    public Log log = LogFactory.getLog(KLDivergenceWeightOptimizer.class);
    public double tau;


    public KLDivergenceWeightOptimizer(SADomain domain, RewardVectorFunction rf, double gamma,
                                       HashableStateFactory hashingFactory, double maxDelta,
                                       int maxIterations, int vectorSize, Comparator vectorComp,
                                       Map<HashableState, Set<Action>> correctPolicy, State initialState, double learningRate, double epsilon) {
        super(domain, gamma, hashingFactory, maxDelta, maxIterations, new HashSet<>(), rf, vectorSize, vectorComp);
        this.correctPolicy = correctPolicy;
        this.initialState = initialState;
        this.learningRate = learningRate;
        this.tau = makeTau(epsilon);
        this.weightGradients = new DefaultHashMap<>(new ArrayRealVector(size,0.));
        this.objective = new DefaultHashMap<>(0d);
    }

    public KLDivergenceWeightOptimizer(SADomain domain, RewardVectorFunction rf, double gamma,
                                       HashableStateFactory hashingFactory, double maxDelta, int maxIterations,
                                       Set<HashableState> noUpdate, int vectorSize,
                                       Comparator vectorComp, Map<HashableState,Set<Action>> correctPolicy, State initialState,
                                       double learningRate, double epsilon) {
        super(domain, gamma, hashingFactory, maxDelta, maxIterations, noUpdate, rf, vectorSize, vectorComp);
        this.correctPolicy = correctPolicy;
        this.initialState = initialState;
        this.learningRate = learningRate;
        this.tau = makeTau(epsilon);

        System.err.println(tau);
        this.weightGradients = new DefaultHashMap<>(new ArrayRealVector(size,0.));
        this.objective = new DefaultHashMap<>(0d);
        Logger.getLogger(KLDivergenceWeightOptimizer.class).setLevel(Level.WARN);
    }

    public KLDivergenceWeightOptimizer(SADomain domain, RewardVectorFunction rf, double gamma,
                                       HashableStateFactory hashingFactory, double maxDelta, int maxIterations,
                                       Set<HashableState> noUpdate, int vectorSize,
                                       Comparator vectorComp, Map<HashableState,Set<Action>> correctPolicy, boolean ignoreTF,
                                       State initialState, double learningRate, double epsilon) {
        super(domain, gamma, hashingFactory, maxDelta, maxIterations, noUpdate, rf, vectorSize, vectorComp, ignoreTF);
        this.correctPolicy = correctPolicy;
        this.initialState = initialState;
        this.learningRate = learningRate;
        this.tau = makeTau(epsilon);
        this.weightGradients = new DefaultHashMap<>(new ArrayRealVector(size,0.));
        this.objective = new DefaultHashMap<>(0d);
    }

    public static double makeTau(double epsilon) {
        return Math.log((1.-epsilon)/epsilon);
    }

//    TODO: implement ignoring the actual norm settings and comparing the policies directly, etc.
    public void optimizeWeights() {

         currentWeights = new ArrayRealVector(size, 1.).mapDivide(size);

        this.performReachabilityFrom(initialState);

        Set<HashableState> states = valueFunction.keySet();

        states.removeAll(noUpdate);

        if(noUpdate.contains(hashingFactory.hashState(initialState))) {

            this.objective.put(hashingFactory.hashState(initialState),Double.POSITIVE_INFINITY);
            return;
        }

        int i;
        for(i = 0; i < this.maxIterations; i++){
            double oldObjective = this.objectiveValue();
            ((WeightedSumComparator)vectorCompare).weights = currentWeights;
            double delta = 0.;
            for(HashableState sh : states){
                RealVector v = this.value(sh);
                RealVector maxQ = this.performBellmanUpdateOn(sh);
                double distance = v.getDistance(maxQ);
                delta = Math.max(distance, delta);

            }


            Map<HashableState,RealVector> tempWeightGradients = new DefaultHashMap<>(new ArrayRealVector(size));
            EnumerablePolicy pol = new RestrictedRandomPolicy(domain,correctPolicy,hashingFactory);
            for(HashableState hs : states) {
                List<Action> actions = this.applicableActions(hs.s());
                RealMatrix qs = new Array2DRowRealMatrix(actions.size(),this.size);
                RealVector correctPolicyVector = new ArrayRealVector(actions.size());
                if(hs.s().equals(initialState)) {
                    qs.setRowVector(0, this.qValue(hs.s(), actions.get(0)));
                    correctPolicyVector.setEntry(0, 1.);
                } else {
                    IntStream.range(0, actions.size()).forEach(index ->
                            {
                                qs.setRowVector(index, this.qValue(hs.s(), actions.get(index)));

                                correctPolicyVector.setEntry(index, PolicyUtils.actionProbGivenDistribution(actions.get(index),
                                        pol.policyDistribution(hs.s())));
                            }
                    );
                }
                RealVector softmaxPolicyVector = softmaxDot(qs,currentWeights, this.tau);
                double klDiv = 0.;
                RealVector newWeightGradientForState = new ArrayRealVector(size);
                if(correctPolicy.containsKey(hs)) {
                    klDiv = divergence(qs, currentWeights, correctPolicyVector, this.tau);
                    newWeightGradientForState = divergenceGradient(qs, currentWeights, correctPolicyVector, this.tau);
                    log.info("----next state----");
                    log.info("QS: " + qs);
                    log.info(softmaxPolicyVector + " vs " + correctPolicyVector);
                    log.info("weightGradient: " +  newWeightGradientForState);
                    log.info("KLDiv at state: " + klDiv);
                }


                for(int index = 0; index < actions.size(); index++) {
                    final double actionProb = correctPolicyVector.getEntry(index);
                    List<TransitionProb> tps = ((FullModel)model).transitions(hs.s(),new CRDRAAction(actions.get(index),size));
                    for(TransitionProb tp : tps) {
                        State sp = getBestStateTransition(hs.s(), actions.get(index), ((CRDRAProductState) tp.eo.op).s).getMiddle();
                        HashableState hsp = hashingFactory.hashState(sp);
                        newWeightGradientForState = newWeightGradientForState.add(weightGradients
                                .get(hsp).mapMultiply(this.gamma * tp.p * actionProb));

                        klDiv += this.gamma * tp.p * actionProb * objective.get(hsp);
                    }
                }
                tempWeightGradients.put(hs,newWeightGradientForState);
                if(hs.equals(hashingFactory.hashState(initialState))) {
                    log.info("Final KLDIV: " + klDiv);
                    log.info("weight gradient: " + newWeightGradientForState);
                }
                log.info("weight gradient for state: " + newWeightGradientForState);
                objective.put(hs,klDiv);

            }


            weightGradients = tempWeightGradients;

            log.info("  total KL divergence " + this.objectiveValue());

            RealVector newWeights;
            try {
                RealVector grad = weightGradients.get(hashingFactory.hashState(initialState));
                log.info("Grad before projection: " + grad);
                grad = grad.subtract(grad.projection(new ArrayRealVector(size,1)));
                log.info("Grad after projection: " + grad);
                    newWeights = currentWeights
                            .subtract(grad.mapMultiply(this.learningRate))
                            .map(d -> Math.max(d, 0.));
                    double norm = newWeights.getL1Norm();
                    if(norm == 0) {
                        throw new RuntimeException("blahblah");
                    }
                    newWeights = newWeights.mapDivide(norm);
            } catch(Exception e) {
                newWeights = currentWeights.copy();
            }
            delta = Math.max(delta, newWeights.getDistance(currentWeights));
            log.info("delta: " + delta);
            currentWeights = newWeights;
            log.warn("itn " + i + " updated weights; new weights: " + currentWeights);
            log.warn("Current objective: " + this.objectiveValue());
            DPrint.cl(this.debugCode, "Finished iteration " + i +", delta: " + delta);
            if(delta < this.maxDelta){
                break; //approximated well enough; stop iterating
            }

        }

        log.warn("Passes: " + i);

        this.hasRunVI = true;

    }

    public double objectiveValue() {
        return this.objective.get(hashingFactory.hashState(initialState));
    }

//    Subtracted min value to make the calculation stable.
    public static RealVector exponentialVec(RealMatrix as, RealVector weight) {
        RealVector weightedSum = as.operate(weight);
        return weightedSum.mapSubtract(weightedSum.getMinValue()).map(a->Math.exp(a));
    }

    public static RealVector softmaxDot(RealMatrix as, RealVector weight, double tau) {
        RealVector g = exponentialVec(as, weight.mapMultiply(tau));
        return g.mapDivide(g.getL1Norm());
    }

    public static double klDivergence(RealVector p, RealVector q) {
        RealVector logQuotient = p.map(d->d == 0. ? 1. : d).map(Math::log).subtract(q.map(Math::log));
        return p.dotProduct(logQuotient);
    }

    public static double divergence(RealMatrix as, RealVector weight, RealVector correctPolicy, double tau) {
        return klDivergence(correctPolicy, softmaxDot(as, weight, tau));
    }

    public static RealVector divergenceGradient(RealMatrix as, RealVector weight, RealVector correctPolicy, double tau) {
        RealMatrix jacobian = new Array2DRowRealMatrix(as.getRowDimension(), as.getColumnDimension());
        RealVector g = exponentialVec(as,weight.mapMultiply(tau));
        double gSum = g.getL1Norm();
        for(int i =0; i < as.getRowDimension(); i++) {
            for(int l = 0; l < as.getColumnDimension(); l++) {
                jacobian.setEntry(i,l, (as.getEntry(i,l) - g.dotProduct(as.getColumnVector(l))/gSum));
            }
        }
        RealVector result = jacobian.preMultiply(correctPolicy).mapMultiply(-1.).mapMultiply(tau);
        return result;

    }

}
