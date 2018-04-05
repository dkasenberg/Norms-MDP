/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.mdp;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.oo.OOSADomain;
import normativeagents.actions.RestrictedActionType;

import java.util.List;

/**
 *
 * @author dkasenberg
 */
public class RestrictedMDP extends WrappedMDPContainer {
    
    public RestrictedMDP(MDPContainer origMDP) {
        super(origMDP);
        this.domain = restrictActionsOnDomain(mdp.domain, null);
        this.initialState = origMDP.initialState.copy();
    }
    
    public SADomain restrictActionsOnDomain(SADomain d, List<Action> bestActions) {

        SADomain newDomain;

        if(d instanceof OOSADomain) {
            newDomain = new OOSADomain();
            OOSADomain orig = (OOSADomain)d;
            for(PropositionalFunction pf : orig.propFunctions()) {
                ((OOSADomain)newDomain).addPropFunction(pf);
            }
            // TODO add state classes somehow?
        } else {
            throw new RuntimeException("Need an OOMDP for product functionality at this point");
        }

        d.getActionTypes().stream().forEach(aT -> {
            newDomain.addActionType(new RestrictedActionType(aT, bestActions, hashingFactory));
        });

        newDomain.setModel(d.getModel());
        this.domain = newDomain;
        
        return newDomain;
    }
    
    
}
