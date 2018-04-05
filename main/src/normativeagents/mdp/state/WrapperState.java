package normativeagents.mdp.state;

import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dan on 5/17/17.
 */
public abstract class WrapperState implements OOState {
    public State s;

    public WrapperState() {

    }

    public WrapperState(State s) {
        this.s = s;
    }

    @Override
    public int numObjects() {
        if(s instanceof OOState) return ((OOState) s).numObjects();
        return 0;
    }

    @Override
    public ObjectInstance object(String oname) {
        if(s instanceof OOState) return ((OOState)s).object(oname);
        return null;
    }

    @Override
    public List<ObjectInstance> objects() {
        if(s instanceof OOState) return ((OOState)s).objects();
        return new ArrayList<>();
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(s instanceof OOState) return ((OOState)s).objectsOfClass(oclass);
        return new ArrayList<>();
    }

    @Override
    public List<Object> variableKeys() {
        List<Object> keys = new ArrayList<>(s.variableKeys());
        keys.addAll(uniqueKeys());
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        return s.get(variableKey);
    }

    public abstract List<Object> uniqueKeys();
}
