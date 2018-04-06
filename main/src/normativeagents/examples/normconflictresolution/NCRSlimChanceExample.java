package normativeagents.examples.normconflictresolution;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import normativeagents.RestrictedQ;
import normativeagents.normconflictresolution.NormConflictResolver;
import normativeagents.single.domains.slimchance.SlimChanceDomain;
import normativeagents.single.domains.slimchance.SlimChanceState;
import normativeagents.statehashing.HashableWrapperStateFactory;

import java.io.PrintStream;

import static normativeagents.single.domains.slimchance.SlimChanceDomain.VAR_GOOD;

/**
 * Created by dkasenberg on 4/6/18.
 */
public class NCRSlimChanceExample {

    public static void main(String[] args) {
        NCRSlimChanceExample example = new NCRSlimChanceExample();
        example.runExample(0.01);
    }

    protected void runExample(double probOfSuccess) {
        SlimChanceDomain sw = new SlimChanceDomain(probOfSuccess);
        OOSADomain d0 = sw.generateDomain();
        State s0 = new SlimChanceState(false);

        String normText = "G good";
        HashableStateFactory hf = new HashableWrapperStateFactory();

        NormConflictResolver ncr = new NormConflictResolver(normText, d0, s0, hf, 0.99);
        ncr.initialize();

        OOSADomain d = (OOSADomain)ncr.getCurrentDomain();

        Environment env = ncr.getEnvironment();

        LearningAgent agent = new RestrictedQ(d, 0.99, hf, 0.3, 0.1, 5);
        int numEpisodes = 3;

//        System.setOut(origOut);

        PrintStream trajout = System.out;
        for(int i = 0; i < numEpisodes; i++) {
            Episode ea = agent.runLearningEpisode(env, 10);
            env.resetEnvironment();
            for(int t = 0; t < ea.numTimeSteps()-1; t++) {
                trajout.print("(");
                trajout.print(StateUtilities.stringOrBoolean(ea.state(t).get(VAR_GOOD)) ? "GOOD" : "BAD");
                trajout.print(",");
                trajout.print(ea.action(t));
                trajout.print("); ");
            }
            trajout.print(StateUtilities.stringOrBoolean(ea.state(ea.maxTimeStep()).get(VAR_GOOD)) ? "GOOD" : "BAD");
            trajout.println();
        }
    }

}
