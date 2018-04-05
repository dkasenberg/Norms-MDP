package normativeagents.mdp.state;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.state.State;

import java.util.Arrays;
import java.util.List;

/**
 * Created by dan on 5/16/17.
 */
public class RecordedActionState extends WrapperState {
    public Action lastAction;
    public static String LAST_ACTION = "_ACTION";
    public static Action actionNull = new SimpleAction();

    public RecordedActionState() {

    }

    public RecordedActionState(State state, Action lastAction) {
        super(state);
        if(lastAction == null) {
            lastAction = actionNull;
        }
        this.lastAction = lastAction;
    }

    public List<Object> uniqueKeys() {
        return Arrays.asList(LAST_ACTION);
    }

    @Override
    public Object get(Object variableKey) {
        if(variableKey.equals(LAST_ACTION)) return lastAction;
        return s.get(variableKey);
    }

    @Override
    public String toString() {
        return "RecordedActionState(\n" + s.toString() + "\n, " + this.lastAction +")";
    }

    @Override
    public State copy() {
        return new RecordedActionState(s.copy(), lastAction);
    }
}
