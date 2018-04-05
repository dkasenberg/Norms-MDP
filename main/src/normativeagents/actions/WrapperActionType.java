/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.actions;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;

import java.util.List;

/**
 *
 * A class for ActionTypes that wrap other ActionTypes.  Useful for CRDRAActionType, RestrictedActionType, etc.
 * @author dkasenberg
 */
public class WrapperActionType implements ActionType {
    public ActionType actionType;
    
    public WrapperActionType(ActionType a) {
        this.actionType = a;
    }

    @Override
    public String typeName() {
        return actionType.typeName();
    }

    @Override
    public Action associatedAction(String strRep) {
        return actionType.associatedAction(strRep);
    }

    @Override
    public List<Action> allApplicableActions(State s) {
        return actionType.allApplicableActions(s);
    }
}
