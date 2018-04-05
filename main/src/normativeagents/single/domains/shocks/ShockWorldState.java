package normativeagents.single.domains.shocks;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static normativeagents.single.domains.shocks.ShockDomain.*;

/**
 * Created by dan on 5/16/17.
 */
public class ShockWorldState implements MutableOOState {

    public Map<String, ShockWorldVictim> victims = new HashMap<>();

    public ShockWorldState() {

    }

    public ShockWorldState(List<ShockWorldVictim> victims) {
        this.victims = victims.stream().collect(Collectors.toMap(v -> v.name, v -> v));
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        if(!(o instanceof ShockWorldVictim)) throw new RuntimeException("Object added must be a victim");
        ShockWorldVictim victim = (ShockWorldVictim)o;
        this.victims.put(victim.name, victim);
        return this;
    }

    @Override
    public MutableOOState removeObject(String oname) {
        this.victims.remove(oname);
        return this;
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {
        ShockWorldVictim ob = victims.get(objectName);
        if(ob != null) {
            victims.remove(objectName);
            victims.put(newName, (ShockWorldVictim)ob.copyWithName(newName));
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
        List<ObjectInstance> obs = new ArrayList<>();
        obs.addAll(victims.values());
        return obs;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(CLASS_VICTIM)){
            return new ArrayList<ObjectInstance>(victims.values());
        }
        throw new RuntimeException("Unknown class type " + oclass);
    }

    @Override
    public MutableState set(Object variableKey, Object value) {

        OOVariableKey key = OOStateUtilities.generateKey(variableKey);
        boolean boolVal = StateUtilities.stringOrBoolean(value);

        ShockWorldVictim newVictim = victims.get(key.obName).copy();
        if(key.obVarKey.equals(VAR_MAIN)){
            newVictim.main = boolVal;
        }
        else if(key.obVarKey.equals(VAR_ALT)){
            newVictim.alternate = boolVal;
        }
        else if(key.obVarKey.equals(VAR_PAIN)){
            newVictim.inPain = boolVal;
        }
        else{
            throw new RuntimeException("Unknown variable key " + variableKey);
        }
        victims.put(newVictim.name, newVictim);

        return this;
    }

    @Override
    public String toString() {
        String s = "";
        for(ShockWorldVictim victim : victims.values()) {
            if(victim.main) {
                s = s + "^";
            } else if(victim.alternate) {
                s = s + "v";
            } else if(victim.inPain) {
                s = s + "X";
            } else {
                s = s + "O";
            }
        }
        return s;
    }

    @Override
    public List<Object> variableKeys() {
        return OOStateUtilities.flatStateKeys(this);
    }

    @Override
    public Object get(Object variableKey) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);
        if(!victims.containsKey(key.obName)) throw new RuntimeException("Cannot find object " + key.obName);
        return victims.get(key.obName).get(key.obVarKey);
    }

    @Override
    public State copy() {
        return new ShockWorldState(new ArrayList<>(victims.values()));
    }
}
