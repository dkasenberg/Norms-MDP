/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.norminference;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import normativeagents.normconflictresolution.NormConflictResolver;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author dkasenberg
 */
public class EpisodeNormInference extends NormInference {
    
    public EpisodeNormInference(OOSADomain d, State s, HashableStateFactory hf, Set<Episode> observedBehavior) {
        this(d, s, hf, observedBehavior, d.propFunctions().stream().map(pf -> pf.getName()).collect(Collectors.toList()));
    }
    
    public EpisodeNormInference(OOSADomain d, State s, HashableStateFactory hf, Set<Episode> observedBehavior, Collection<String> props) {
        super(d,s,hf,observedBehavior,props);
    }

    @Override
    protected double computeObjX(NormConflictResolver ncr, Collection<Episode> trajectories) {

        RealVector randomViol = ncr.getViolationCostForRandomPolicy();

        RealVector behaviorViol = new ArrayRealVector(randomViol.getDimension());

        for(Episode ea : observedBehavior) {
            behaviorViol =behaviorViol.add(ncr.getViolationCostForEpisode(ea.stateSequence));
        }

        RealVector weights = new ArrayRealVector(behaviorViol.getDimension(),1.);

        return weights.dotProduct(behaviorViol.subtract(randomViol));
    }
}
