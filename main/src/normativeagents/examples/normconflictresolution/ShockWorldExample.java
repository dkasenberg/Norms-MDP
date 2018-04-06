package normativeagents.examples.normconflictresolution;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import normativeagents.RestrictedQ;
import normativeagents.normconflictresolution.NormConflictResolver;
import normativeagents.single.domains.shocks.ShockDomain;
import normativeagents.single.domains.shocks.ShockWorldVictim;
import normativeagents.statehashing.HashableWrapperStateFactory;

/**
 * Created by dkasenberg on 4/6/18.
 */
public class ShockWorldExample {

    public static void main(String[] args) {
        ShockWorldExample example = new ShockWorldExample();
        example.runExample(2);
    }

    protected void runExample(int numVictims) {

        ShockDomain sd = new ShockDomain();
        OOSADomain d = sd.generateDomain();
        OOState s = ShockDomain.oneAgentNoVictims(d);
        for(int i =0; i < numVictims; i++) {
            ShockDomain.addVictim(d, s);
        }

        String formula = "1.0: G ! inPain(x)";

        HashableStateFactory hf = new HashableWrapperStateFactory();

        NormConflictResolver ncr = new NormConflictResolver(formula, d, s, hf,0.99);

        ncr.initialize();

        Environment env = ncr.getEnvironment();

        LearningAgent agent = new RestrictedQ(ncr.getCurrentDomain(), 0.99, hf, 0.3, 0.1, 5);
        int numEpisodes = 1;

        for(int i = 0; i < numEpisodes; i++) {
            Episode ea = agent.runLearningEpisode(env, 50);

            for(int j = 0; j < ea.stateSequence.size()-1; j++) {
                OOState st = (OOState)ea.state(j);
                if(!st.objectsOfClass(ShockDomain.CLASS_VICTIM)
                        .stream()
                        .anyMatch(v -> ((ShockWorldVictim)v).main ||
                                ((ShockWorldVictim)v).alternate)) {
                    continue;
                } else if(ea.actionSequence.size() <= j) {
                    continue;
                }
                System.out.print(st.objectsOfClass(ShockDomain.CLASS_VICTIM)
                        .stream()
                        .filter(v -> ((ShockWorldVictim)v).main)
                        .count());
                System.out.print("\t");
                System.out.print(ea.actionSequence.get(j));
                System.out.print("\t");
                System.out.print(((OOState)ea.stateSequence
                        .get(j+1))
                        .objectsOfClass(ShockDomain.CLASS_VICTIM)
                        .stream()
                        .filter(v -> ((ShockWorldVictim)v).inPain)
                        .count());
                System.out.println();
            }
        }
    }
}
