/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.norminference;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import normativeagents.RestrictedRandomPolicy;
import normativeagents.actions.CRDRAAction;
import normativeagents.normconflictresolution.NormConflictResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author dkasenberg
 */
public class PolicyNormInference extends NormInference {

    protected Log log = LogFactory.getLog(PolicyNormInference.class);
    
    public PolicyNormInference(OOSADomain d, State s, HashableStateFactory hf, Collection<Episode> observedBehavior) {
        this(d, s, hf, observedBehavior, d.propFunctions().stream().map(pf -> pf.getName()).collect(Collectors.toList()));
    }

    public PolicyNormInference(OOSADomain d, State s, HashableStateFactory hf, Collection<Episode> observedBehavior,
                               Collection<String> props) {
        super(d,s,hf,observedBehavior,props);
    }

    @Override
    protected double computeObjX(NormConflictResolver ncr, Collection<Episode> trajectories) {
        OOSADomain newD = (OOSADomain)ncr.product.domain;

        // - get state sequences in product space from norm system
        // - construct policy that is random but restricted to actions observed
        //   in observed states
        // - get unsafety of that policy according to the getViolationCostForPolicy() function.

        Map<HashableState, Set<Action>> partialPolicy = new HashMap<>();
        for(Episode ea : observedBehavior) {
            List<State> pStateSequence = ncr.getProductSpaceStateSequence(ea.stateSequence);
            if(pStateSequence == null) {
                continue;
            }
            for(int i = 1; i < pStateSequence.size()-1; i++) {
                HashableState hs = hashingFactory.hashState(pStateSequence.get(i));
                Action ga = ea.action(i-1);
                ga = new CRDRAAction(ga, new int[ncr.normInstances.size()]);
                if(partialPolicy.containsKey(hs)) {
                    partialPolicy.get(hs).add(ga);
                } else {
                    partialPolicy.put(hs, new HashSet<>(Collections.singleton(ga)));
                }
            }
        }
        log.info("----");
        RealVector behaviorViol = ncr.getViolationCostForPolicy(new RestrictedRandomPolicy(newD, partialPolicy, hashingFactory));
        log.info("Observed policy: " + behaviorViol);
        RealVector randomViol = ncr.getViolationCostForRandomPolicy();
        log.info("Random policy: " + randomViol);

        RealVector weights = new ArrayRealVector(behaviorViol.getDimension(),1.);

        return weights.dotProduct(behaviorViol.subtract(randomViol));
    }
}
