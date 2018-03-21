package normativeagents.single.domains.vacuum.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import normativeagents.single.domains.MutableObjectInstance;

import java.util.Arrays;
import java.util.List;

import static normativeagents.single.domains.vacuum.state.VacuumState.*;

/**
 * Created by dkasenberg on 8/3/17.
 */
public class VacuumObstacle implements MutableObjectInstance {

    protected String name;

    public int x;
    public int y;
    public int value;
    public boolean breakable;
    public boolean solid;


    public static final String VAR_VALUE = "value";
    public static final String VAR_BREAKABLE = "breakable";
    public static final String VAR_SOLID = "solid";

    protected List<Object> keys = Arrays.asList(VAR_VALUE, VAR_BREAKABLE, VAR_X_POS, VAR_Y_POS, VAR_SOLID);

    public VacuumObstacle(String name, int value, boolean breakable, boolean solid, int x, int y) {
        this.name = name;
        this.value = value;
        this.breakable = breakable;
        this.x = x;
        this.y = y;
        this.solid = solid;
    }

    @Override
    public String className() {
        return CLASS_OBSTACLE;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public ObjectInstance copyWithName(String s) {
        return new VacuumObstacle(s, this.value, this.breakable, this.solid, this.x, this.y);
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object key) {
        if(key.equals(VAR_BREAKABLE)) {
            return breakable;
        } else if(key.equals(VAR_VALUE)) {
            return value;
        } else if(key.equals(VAR_X_POS)) {
            return x;
        } else if(key.equals(VAR_Y_POS)) {
            return y;
        } else if(key.equals(VAR_SOLID)) {
            return this.solid;
        }
        throw new RuntimeException("[" + this.name + ":get] key " + key + " not found");
    }

    @Override
    public State copy() {
        return new VacuumObstacle(this.name, this.value, this.breakable, this.solid, this.x, this.y);
    }

    @Override
    public void set(Object key, Object value) {
        Number numValue = StateUtilities.stringOrNumber(value);
        boolean boolValue = StateUtilities.stringOrBoolean(value);
        if(key.equals(VAR_BREAKABLE)) {
            this.breakable = boolValue;
        } else if(key.equals(VAR_VALUE)) {
            this.value = numValue.intValue();
        } else if(key.equals(VAR_X_POS)) {
            this.x = numValue.intValue();
        } else if(key.equals(VAR_Y_POS)) {
            this.y = numValue.intValue();
        } else if(key.equals(VAR_SOLID)) {
            this.solid = boolValue;
        }
        throw new RuntimeException("[" + this.name + ":get] key " + key + " not found");
    }
}
