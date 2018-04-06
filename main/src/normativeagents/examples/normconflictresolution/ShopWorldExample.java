package normativeagents.examples.normconflictresolution;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import normativeagents.RestrictedQ;
import normativeagents.normconflictresolution.NormConflictResolver;
import normativeagents.single.domains.shopworld.ShopWorldDomain;
import normativeagents.single.domains.shopworld.state.ShopWorldState;
import normativeagents.single.domains.shopworld.state.ShopWorldTrinket;
import normativeagents.statehashing.HashableWrapperStateFactory;

/**
 * Created by dkasenberg on 4/6/18.
 */
public class ShopWorldExample {


    public static void main(String[] args) {
        ShopWorldExample example = new ShopWorldExample();
        example.runExample();
    }

    protected void runExample() {

        ShopWorldDomain sw = new ShopWorldDomain(0.7,0.3,0.5,0.7, -10, -0.5);
        OOSADomain d0 = sw.generateDomain();
        ShopWorldState s0 = ShopWorldDomain.oneAgentNoTrinkets(d0, 100);
        ShopWorldDomain.addTrinket(d0, s0, 30.0, 45.0, ShopWorldTrinket.Size.MEDIUM);

        String normText = "1.0:G ! caughtStealing(x)";
        HashableStateFactory hf = new HashableWrapperStateFactory();
//        hf = new SimpleHashableStateFactory(false);
        NormConflictResolver ncr = new NormConflictResolver(normText, d0, s0, hf, 0.99);
        ncr.initialize();

        Environment env = ncr.getEnvironment();

        LearningAgent agent = new RestrictedQ(ncr.getCurrentDomain(), 0.99, hf, 0.3, 0.1, 5);
        int numEpisodes = 5000;

        for(int i = 0; i < numEpisodes; i++) {
            Episode ea = agent.runLearningEpisode(env, 50);
            env.resetEnvironment();
            System.out.println(i + "\t" + ea.actionString());
        }
    }

}
