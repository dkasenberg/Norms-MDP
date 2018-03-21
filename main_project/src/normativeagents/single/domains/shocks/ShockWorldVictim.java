package normativeagents.single.domains.shocks;

import burlap.mdp.core.oo.state.ObjectInstance;

import java.util.Arrays;
import java.util.List;

import static normativeagents.single.domains.shocks.ShockDomain.*;

/**
 * Created by dan on 5/16/17.
 */
public class ShockWorldVictim implements ObjectInstance {

    public boolean main;
    public boolean alternate;
    public boolean inPain;

    public String name;

    private final static List<Object> keys = Arrays.asList(VAR_MAIN, VAR_ALT, VAR_PAIN);

    public ShockWorldVictim(String name) {
        this.name = name;
        this.main = false;
        this.alternate = false;
        this.inPain = false;
    }

    public ShockWorldVictim(String name, boolean main, boolean alternate, boolean inPain) {
        this.name = name;
        this.main = main;
        this.alternate = alternate;
        this.inPain = inPain;
    }


    @Override
    public String className() {
        return CLASS_VICTIM;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new ShockWorldVictim(objectName, main, alternate, inPain);
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if(!(variableKey instanceof String)){
            throw new RuntimeException("ShopWorldTrinket variable key must be a string");
        }

        String key = (String)variableKey;
        if(key.equals(VAR_MAIN)){
            return main;
        }
        else if(key.equals(VAR_ALT)){
            return alternate;
        }
        else if(key.equals(VAR_PAIN)) {
            return inPain;
        }

        throw new RuntimeException("Unknown key " + key);
    }

    @Override
    public ShockWorldVictim copy() {
        return new ShockWorldVictim(name, main, alternate, inPain);
    }
}
