/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.actions;


import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An actionType with restrictions on the set of actions available in certain states.  This is allows us to dynamically
 * restrict the actions available within different product states in NCR.
 *
 * @author dkasenberg
 */
public class RestrictedActionType extends WrapperActionType {

    /**
     * A map from individual states to which actions should be allowed in them.
     */
    private Map<HashableState, List<Action>> specificApplicableActions;

    /**
     * A list of which actions are allowed everywhere.  If not null, only the actions in this list may be performed.
     */
    private List<Action> generalApplicableActions;
    protected HashableStateFactory hashingFactory;
    
    public RestrictedActionType(ActionType a, List<Action> applicableActions, HashableStateFactory hf) {
        super(a);
        this.hashingFactory = hf;
        this.generalApplicableActions = applicableActions;
    }

    /**
     * Enforces the restriction that only the given set of actions should be performed.
     * @param actions a list of actions
     * */
    public void setActionRestriction(List<Action> actions) {
        this.generalApplicableActions = actions;
    }

    /**
     * Enforces the restriction that only the given set of actions should be performed in the corresponding states.
     * @param appActions a mapping from states to the actions that are permissible in those states.
     * */
    public void setSpecificActionRestriction(Map<HashableState, List<Action>> appActions) {
        this.specificApplicableActions = appActions;
    }

    @Override
    public Action associatedAction(String strRep) {
        return actionType.associatedAction(strRep);
    }

    @Override
    public List<Action> allApplicableActions(State s) {
        HashableState hs = hashingFactory.hashState(s);

        return actionType.allApplicableActions(s).stream().filter(a -> (generalApplicableActions == null ||
                generalApplicableActions.contains(a)) &&
                (specificApplicableActions == null ||
                        !specificApplicableActions.containsKey(hs) ||
                        specificApplicableActions.get(hs).contains(a))).collect(Collectors.toList());
    }

}
