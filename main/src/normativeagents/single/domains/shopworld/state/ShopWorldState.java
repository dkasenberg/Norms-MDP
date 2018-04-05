package normativeagents.single.domains.shopworld.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;

import java.util.*;

import static normativeagents.single.domains.shopworld.ShopWorldDomain.*;

/**
 * Created by dan on 5/15/17.
 */
public class ShopWorldState implements MutableOOState {

    public ShopWorldAgent agent;
    protected Map<String, ShopWorldTrinket> trinkets = new HashMap<String, ShopWorldTrinket>();

    public ShopWorldState() {}

    public ShopWorldState(double startMoney) {
        this(startMoney, new HashMap<>());
    }

    public ShopWorldState(double startMoney, Map<String, ShopWorldTrinket> trinkets) {
        this(new ShopWorldAgent(startMoney), trinkets);
    }

    public ShopWorldState(ShopWorldAgent agent, Map<String, ShopWorldTrinket> trinkets) {
        this.agent = agent;
        this.trinkets = trinkets;
    }


    @Override
    public MutableOOState addObject(ObjectInstance o) {
        if(!(o instanceof ShopWorldTrinket)) {
            throw new RuntimeException("Can only add ShopWorldTrinket objects to a ShopWorldState.");
        }

        this.trinkets.put(o.name(), (ShopWorldTrinket)o);

        return this;
    }

    @Override
    public MutableOOState removeObject(String oname) {
        if(oname.equals(agent.name())){
            throw new RuntimeException("Cannot remove agent object from state");
        }
        //copy on write
        trinkets.remove(oname);

        return this;
    }

    @Override
    public String toString() {
        return OOStateUtilities.ooStateToString(this);
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {

        if(objectName.equals(agent.name())){
            ShopWorldAgent nagent = agent.copyWithName(newName);
            this.agent = nagent;
        }

        ShopWorldTrinket ob = this.trinkets.get(objectName);
        if(ob != null){
            this.trinkets.remove(objectName);
            this.trinkets.put(newName, ob.copyWithName(newName));
        }


        return this;
    }

    @Override
    public int numObjects() {
        return 1 + this.trinkets.size();
    }

    @Override
    public ObjectInstance object(String oname) {
        if(oname.equals(agent.name())){
            return agent;
        }
        return trinkets.get(oname);
    }

    @Override
    public List<ObjectInstance> objects() {
        List<ObjectInstance> obs = new ArrayList<ObjectInstance>(1+trinkets.size());
        obs.add(agent);
        obs.addAll(trinkets.values());
        return obs;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(CLASS_AGENT)){
            return Arrays.asList(agent);
        }
        else if(oclass.equals(CLASS_TRINKET)){
            return new ArrayList<ObjectInstance>(trinkets.values());
        }
        throw new RuntimeException("Unknown class type " + oclass);
    }

    @Override
    public MutableState set(Object variableKey, Object value) {

        OOVariableKey key = OOStateUtilities.generateKey(variableKey);

        Number numVal = StateUtilities.stringOrNumber(value);
        boolean boolVal = StateUtilities.stringOrBoolean(value);

        if(key.obName.equals(agent.name())){
            if(key.obVarKey.equals(VAR_MONEY)){
                touchAgent().money = numVal.doubleValue();
            }
            else if(key.obVarKey.equals(VAR_LEFT)){
                touchAgent().leftStore = boolVal;
            }
            else if(key.obVarKey.equals(VAR_CAUGHT)) {
                touchAgent().caughtStealing = boolVal;
            }
            else{
                throw new RuntimeException("Unknown variable key " + variableKey);
            }
            return this;
        }
        ShopWorldTrinket newTrinket = trinkets.get(key.obName).copy();
        if(key.obVarKey.equals(VAR_INSTOCK)){
            newTrinket.inStock = boolVal;
        }
        else if(key.obVarKey.equals(VAR_HELD)){
            newTrinket.held = boolVal;
        }
        else if(key.obVarKey.equals(VAR_HIDDEN)){
            newTrinket.hidden = boolVal;
        }
        else if(key.obVarKey.equals(VAR_BOUGHT)){
            newTrinket.bought = boolVal;
        }
        else if(key.obVarKey.equals(VAR_COST)){
            newTrinket.cost = numVal.doubleValue();
        }
        else if(key.obVarKey.equals(VAR_VALUE)){
            newTrinket.value = numVal.doubleValue();
        }
        else if(key.obVarKey.equals(VAR_SIZE)){
            newTrinket.size = ShopWorldTrinket.Size.values()[numVal.intValue()];
        }
        else{
            throw new RuntimeException("Unknown variable key " + variableKey);
        }

        return this;
    }

    @Override
    public List<Object> variableKeys() {
        return OOStateUtilities.flatStateKeys(this);
    }

    public ShopWorldAgent touchAgent(){
        this.agent = agent.copy();
        return agent;
    }

    @Override
    public Object get(Object variableKey) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);
        if(key.obName.equals(agent.name())){
            return agent.get(key.obVarKey);
        }
        if(!trinkets.containsKey(key.obName)) throw new RuntimeException("Cannot find object " + key.obName);
        return trinkets.get(key.obName).get(key.obVarKey);
    }

    @Override
    public State copy() {
        return new ShopWorldState(touchAgent(), new HashMap<>(trinkets));
    }

}
