package normativeagents.single.domains.moralrisk;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;

import java.util.*;

import static normativeagents.single.domains.moralrisk.MoralRiskDomain.*;

/**
 * Created by dan on 5/17/17.
 */
public class MoralRiskState implements MutableOOState {

    public boolean done;
    public Map<String, MoralRiskVictim> victims = new HashMap<>();
    public List<Object> keys = Arrays.asList(VAR_DEAD);

    public MoralRiskState() {
        this.done = false;
    }

    public MoralRiskState(Map<String, MoralRiskVictim> victims) {
        this.victims = victims;
        this.done = false;
    }

    public MoralRiskState(Map<String, MoralRiskVictim> victims, boolean done) {
        this.victims = victims;
        this.done = done;
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        if(o instanceof MoralRiskVictim) {
            victims.put(o.name(), (MoralRiskVictim)o);
            return this;
        }
        throw new RuntimeException("Object added must be a victim.");
    }

    @Override
    public MutableOOState removeObject(String oname) {
        victims.remove(oname);
        return this;
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {
        MoralRiskVictim obj = victims.get(objectName);
        if(obj != null) {
            victims.remove(objectName);
            victims.put(newName, (MoralRiskVictim)obj.copyWithName(newName));
        }
        return this;
    }

    @Override
    public int numObjects() {
        return victims.size();
    }

    @Override
    public ObjectInstance object(String oname) {
        return victims.get(oname);
    }

    @Override
    public List<ObjectInstance> objects() {
        return new ArrayList<>(victims.values());
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(!(oclass.equals(CLASS_VICTIM))) {
            throw new RuntimeException("The only objects in this domain are victims.");
        }
        return new ArrayList<>(victims.values());
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        if(variableKey.equals(VAR_DONE)) {
            this.done = StateUtilities.stringOrBoolean(value);
        }
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);
        MoralRiskVictim ob = this.victims.get(key.obName);
        if(ob != null){
            ob = (MoralRiskVictim)ob.copy();
            if(key.obVarKey.equals(VAR_DEAD)){
                ob.dead = StateUtilities.stringOrBoolean(value);
            }
            this.victims.put(ob.name(), ob);
        }
        return this;
    }

    @Override
    public List<Object> variableKeys() {
        return OOStateUtilities.flatStateKeys(this);
    }

    @Override
    public Object get(Object variableKey) {
        if(variableKey.equals(VAR_DONE)) return done;
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);
        ObjectInstance ob = this.victims.get(key.obName);
        if(ob == null){
            throw new RuntimeException("Unknown object " + ob.name());
        }
        return ob.get(key.obVarKey);
    }

    @Override
    public State copy() {
        return new MoralRiskState(new HashMap<>(victims), done);
    }
}
