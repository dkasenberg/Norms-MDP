package normativeagents.single.domains.vacuum.state;

import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import normativeagents.single.domains.MutableObjectInstance;

import java.util.Arrays;
import java.util.List;

import static normativeagents.single.domains.vacuum.state.VacuumState.*;

/**
 * Created by dkasenberg on 9/6/17.
 */
public class VacuumDocker implements MutableObjectInstance {
    public String name;
    public int dockingSpeed;
    public int x;
    public int y;

    public static final String VAR_SPEED = "dockingSpeed";
    public static final List<Object> keys = Arrays.asList(VAR_SPEED, VAR_X_POS, VAR_Y_POS);

    public VacuumDocker() {

    }

    public VacuumDocker(String name, int dockingSpeed, int x, int y) {
        this.name = name;
        this.dockingSpeed = dockingSpeed;
        this.x = x;
        this.y = y;
    }

    @Override
    public void set(Object key, Object value) {
        Number numVal = StateUtilities.stringOrNumber(value);
        if(key.equals(VAR_SPEED)) {
            this.dockingSpeed = numVal.intValue();
        } else if(key.equals(VAR_Y_POS)) {
            this.y = numVal.intValue();
        } else if(key.equals(VAR_X_POS)) {
            this.x = numVal.intValue();
        }
        throw new RuntimeException("[" + this.name + ":set] key " + key + " not found");
    }

    @Override
    public String className() {
        return CLASS_DOCKER;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public ObjectInstance copyWithName(String s) {
        return new VacuumDocker(s,this.dockingSpeed, this.x, this.y);
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object o) {
        if(o.equals(VAR_SPEED)) {
            return this.dockingSpeed;
        } else if(o.equals(VAR_Y_POS)) {
            return this.y;
        } else if(o.equals(VAR_X_POS)) {
            return this.x;
        }
        throw new RuntimeException("[" + this.name + ":set] key " + o + " not found");
    }

    @Override
    public State copy() {
        return new VacuumDocker(this.name, this.dockingSpeed, this.x, this.y);
    }

    @Override
    public String toString() {
        return OOStateUtilities.objectInstanceToString(this);
    }
}
