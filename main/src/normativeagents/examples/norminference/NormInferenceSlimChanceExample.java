package normativeagents.examples.norminference;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import normativeagents.RestrictedQ;
import normativeagents.normconflictresolution.NormConflictResolver;
import normativeagents.norminference.PolicyNormInference;
import normativeagents.single.domains.slimchance.SlimChanceDomain;
import normativeagents.single.domains.slimchance.SlimChanceState;
import normativeagents.statehashing.HashableWrapperStateFactory;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.Grammar;
import org.moeaframework.util.distributed.DistributedProblem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static normativeagents.single.domains.slimchance.SlimChanceDomain.VAR_GOOD;

/**
 * Created by dkasenberg on 4/6/18.
 */
public class NormInferenceSlimChanceExample {

    public static void main(String[] args) {

        NormInferenceSlimChanceExample example = new NormInferenceSlimChanceExample();
        example.runExample(0.1);

    }

    private void runExample(double probOfSuccess) {
        Collection<Episode> moralBehaviors;

        String normText = "G good";

        HashableStateFactory hf;

        SlimChanceDomain sw = new SlimChanceDomain(probOfSuccess);
        OOSADomain d0 = sw.generateDomain();
        State s0 = new SlimChanceState();

        hf = new HashableWrapperStateFactory();

        NormConflictResolver normSystem = new NormConflictResolver(normText, d0, s0, hf, 0.99);
        normSystem.initialize();

        OOSADomain d = (OOSADomain)normSystem.getCurrentDomain();

        Environment env = normSystem.getEnvironment();

        LearningAgent agent = new RestrictedQ(d, 0.99, hf, 0.3, 0.1, 5);
        int numEpisodes = 3;

        moralBehaviors = new HashSet<>();
        for(int i = 0; i < numEpisodes; i++) {
            Episode ea = agent.runLearningEpisode(env, 10);
            env.resetEnvironment();
            moralBehaviors.add(ea);
            for(int t = 0; t < ea.numTimeSteps()-1; t++) {
                System.out.print("(");
                System.out.print(StateUtilities.stringOrBoolean(ea.state(t).get(VAR_GOOD)) ? "GOOD" : "BAD");
                System.out.print(",");
                System.out.print(ea.action(t));
                System.out.print("); ");
            }
            System.out.print(StateUtilities.stringOrBoolean(ea.state(ea.maxTimeStep()).get(VAR_GOOD)) ? "GOOD" : "BAD");
            System.out.println();
        }

        Collection<String> props = new ArrayList<>();
        props.add("good");

        ExecutorService exec = Executors.newSingleThreadExecutor();

        PolicyNormInference inf = new PolicyNormInference(d0, s0, hf, moralBehaviors, props);
        DistributedProblem problem = new DistributedProblem(inf, exec);
        long beginTime = System.nanoTime();
        NondominatedPopulation result= new Executor().withProblem(problem)
                .withAlgorithm("NSGAII")
                .withProperty("populationSize", 100)
                .withMaxEvaluations(5000)
                .run();
        long duration = System.nanoTime() -beginTime;

        exec.shutdown();

        for(Solution solution : result) {

            int[] codon = ((Grammar)solution.getVariable(0)).toArray();
            double discount = 0.99;

            String norm = inf.ltlGrammar.build(codon);
            if(solution.getObjective(1) != 0) {
                System.out.println(norm + "\t" + solution.getObjective(0) + "\t"
                        + solution.getObjective(1));
            }
        }
    }

}
