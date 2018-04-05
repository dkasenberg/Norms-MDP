package normativeagents.actions;

import burlap.mdp.core.action.Action;

/**
 * A base class for actions that wrap other actions (useful for CRDRAAction etc)
 * @author dkasenberg
 */
public class WrapperAction implements Action {

    public Action action;

    public WrapperAction(Action action) {
        this.action = action;
    }

    @Override
    public String actionName() {
        return action.actionName();
    }

    @Override
    public int hashCode() {
        return action.hashCode();
    }


    @Override
    public Action copy() {
        return new WrapperAction(action.copy());
    }

    @Override
    public String toString() {
        return action.toString();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof WrapperAction) {
            return action.equals(((WrapperAction)o).action);
        }
        return action.equals(o);
    }
}
