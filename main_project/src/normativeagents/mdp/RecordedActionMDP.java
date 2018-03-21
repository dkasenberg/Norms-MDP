/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.mdp;


import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import normativeagents.mdp.model.RecordedActionModel;
import normativeagents.mdp.state.RecordedActionState;

/**
 *
 * @author dkasenberg
 */
public class RecordedActionMDP extends WrappedMDPContainer {
    
    public RecordedActionMDP(MDPContainer mdp) {
        super(mdp);
        this.domain = recordActionsOnDomain(mdp.domain);
        this.initialState = new RecordedActionState(mdp.initialState, null);
    }
    
    public SADomain recordActionsOnDomain(SADomain d) {
        SADomain newDomain;

        if(d instanceof OOSADomain) {
            newDomain = new OOSADomain();
            OOSADomain orig = (OOSADomain)d;
            for(PropositionalFunction pf : orig.propFunctions()) {
                ((OOSADomain)newDomain).addPropFunction(pf);
            }
            // TODO add state classes somehow?
        } else {
            newDomain = new SADomain();
        }


        newDomain.addActionTypes(d.getActionTypes().toArray(new ActionType[0]));

        FactoredModel fm = (FactoredModel)d.getModel();

        newDomain.setModel(new FactoredModel(new RecordedActionModel(fm.getStateModel()), fm.getRf(), fm.getTf()));
        
        return newDomain;
    }
    
}
