package normativeagents.single.domains.slimchance;

import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;

import java.util.Arrays;
import java.util.List;

import static normativeagents.single.domains.slimchance.SlimChanceDomain.VAR_GOOD;

/**
 * Created by dan on 5/17/17.
 */
public class SlimChanceState implements MutableState {

    public boolean good;

    public SlimChanceState(boolean good) {
        this.good = good;
    }

    public SlimChanceState() {
        this.good = false;
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        if(variableKey.equals(VAR_GOOD)) {
            this.good = StateUtilities.stringOrBoolean(value);
        } else {
            throw new RuntimeException("Key " + variableKey + " not found.");
        }
        return this;
    }

    @Override
    public List<Object> variableKeys() {
        return Arrays.asList(VAR_GOOD);
    }

    @Override
    public Object get(Object variableKey) {
        if(variableKey.equals(VAR_GOOD)) {
            return this.good;
        }
        throw new RuntimeException("Key " + variableKey + " not found.");
    }

    @Override
    public State copy() {
        return new SlimChanceState(good);
    }


    public String toString() {
        return this.good ? "GOOD" : "BAD";
    }
}
