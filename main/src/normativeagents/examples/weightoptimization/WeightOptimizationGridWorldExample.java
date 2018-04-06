package normativeagents.examples.weightoptimization;

import burlap.behavior.singleagent.Episode;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.state.GridAgent;
import burlap.domain.singleagent.gridworld.state.GridLocation;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import normativeagents.RestrictedQ;
import normativeagents.actions.CRDRAAction;
import normativeagents.mdp.WrappedTerminalFunction;
import normativeagents.normconflictresolution.NormConflictResolver;
import normativeagents.rewardvector.WeightedGraphRVF;
import normativeagents.rewardvector.comparator.WeightedSumComparator;
import normativeagents.statehashing.HashableWrapperStateFactory;
import normativeagents.weightoptimization.KLDivergenceWeightOptimizer;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.*;
import java.util.stream.Collectors;

import static burlap.domain.singleagent.gridworld.GridWorldDomain.CLASS_AGENT;
import static burlap.domain.singleagent.gridworld.GridWorldDomain.VAR_X;
import static burlap.domain.singleagent.gridworld.GridWorldDomain.VAR_Y;

/**
 * Created by dkasenberg on 4/6/18.
 */
public class WeightOptimizationGridWorldExample {
    public static void main(String[] args) {
        WeightOptimizationGridWorldExample example = new WeightOptimizationGridWorldExample();
        example.runExample(10);
    }

    private void runExample(int numEpisodes) {
        GridWorldDomain gwd = new GridWorldDomain(3,3);
        gwd.setProbSucceedTransitionDynamics(0.8);
        State s0 = new GridWorldState(new GridAgent(0,0),new GridLocation(0,1,"badLocation"),
                new GridLocation(0,2,"goodLocation"));
        gwd.setTf(new WrappedTerminalFunction(new NullTermination()));

        OOSADomain d0 = gwd.generateDomain();
        HashableStateFactory hf = new HashableWrapperStateFactory();

        String normText = "G atLocation(agent,goodLocation); G ! atLocation(agent,badLocation)";
        NormConflictResolver ncr = new NormConflictResolver(normText, d0, s0, hf, 0.99,false);
        ncr.resetWeights(new ArrayRealVector(new double[]{0.08,0.92}));

        ncr.initialize();
        System.out.println("Initialized norm system");

        Environment env = ncr.getEnvironment();
        RestrictedQ agent = new RestrictedQ(ncr.getCurrentDomain(), 0.99, hf, 0.3, 0.1, 5);

        List<Episode> moralBehaviors = new ArrayList<>();
        for(int i = 0; i < numEpisodes; i++) {
            Episode ea = agent.runLearningEpisode(env, 10);
            for(int j = 0; j < ea.maxTimeStep(); j++) {
                OOState so = (OOState)ea.stateSequence.get(j);
                System.out.print("(" + StateUtilities.stringOrNumber(so.get(new OOVariableKey(CLASS_AGENT,VAR_X))).intValue());
                System.out.print("," + StateUtilities.stringOrNumber(so.get(new OOVariableKey(CLASS_AGENT,VAR_Y))).intValue()+"):");
                System.out.print(ea.actionSequence.get(j) + ";");
            }
            System.out.println();
            env.resetEnvironment();
            moralBehaviors.add(ea);
        }

        String candidateNorms =
                "1.0: G atLocation(agent,badLocation);"
                        +
                        "1.0: G atLocation(agent,goodLocation);"
                        +
                        " 1.0: G ! atLocation(agent,badLocation);"
                        +
                        " 1.0:G ! atLocation(agent,goodLocation)"
                ;
//        String normText = "1.0: G ! inPain(victim1); 1.0: G ((X inPain(victim0)) & (X inPain(victim0)))";
        NormConflictResolver ns2 = new NormConflictResolver(candidateNorms, d0, s0, hf,0.99, true);
        System.out.println(ns2.normInstances);

        ns2.initialize();
        OOSADomain d2 = (OOSADomain)ns2.getCurrentDomain();
        OOState s2 = (OOState)ns2.getStartState();


        Map<HashableState, Set<Action>> partialPolicy = new HashMap<>();
        for(Episode ea : moralBehaviors) {
            List<State> pStateSequence = ns2.getProductSpaceStateSequence(ea.stateSequence);
            if(pStateSequence == null) {
                continue;
            }
            for(int i = 1; i < pStateSequence.size()-1; i++) {
                HashableState hs = hf.hashState(pStateSequence.get(i));
                Action ga = ea.action(i-1);
                ga = new CRDRAAction(ga, new int[2]);
                if(partialPolicy.containsKey(hs)) {
                    partialPolicy.get(hs).add(ga);
                } else {
                    partialPolicy.put(hs, new HashSet<>(Collections.singleton(ga)));
                }
            }
        }

        KLDivergenceWeightOptimizer weightOptimizer = new KLDivergenceWeightOptimizer(ns2.product.domain,
                new WeightedGraphRVF(ns2.weightedGraph, hf),
                0.99,
                hf, 1E-6, 50000,
                ns2.noUpdate,
                ns2.normInstances.size(),
                new WeightedSumComparator(ns2.normInstances.stream().map(ni -> ni.crdra)
                        .collect(Collectors.toList())),
                partialPolicy

                , ns2.product.initialState, 1E-6,1E-60);

        weightOptimizer.optimizeWeights();
        System.out.println(weightOptimizer.objectiveValue());
        System.out.println(weightOptimizer.currentWeights);
        ns2.resetWeights(weightOptimizer.currentWeights);
//
        env = ns2.getEnvironment();
        env.resetEnvironment();

        agent = new RestrictedQ(ns2.getCurrentDomain(), 0.99, hf, 0.3, 0.1, 5);
        for(int i = 0; i < 1000; i++) {
            Episode ea = agent.runLearningEpisode(env, 50);
            for(int j = 0; j < ea.maxTimeStep(); j++) {
                OOState so = (OOState)ea.stateSequence.get(j);
                System.out.print("(" + StateUtilities.stringOrNumber(so.get(new OOVariableKey(CLASS_AGENT,VAR_X))).intValue());
                System.out.print("," + StateUtilities.stringOrNumber(so.get(new OOVariableKey(CLASS_AGENT,VAR_Y))).intValue()+"):");
                System.out.print(ea.actionSequence.get(j) + ";");
            }
            System.out.println();
            env.resetEnvironment();
        }

    }

}
