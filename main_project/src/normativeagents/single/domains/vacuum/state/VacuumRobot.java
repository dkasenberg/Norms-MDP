package normativeagents.single.domains.vacuum.state;

import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import normativeagents.single.domains.MutableObjectInstance;

import java.util.Arrays;
import java.util.List;

import static normativeagents.single.domains.vacuum.state.VacuumState.*;

/**
 * Created by dkasenberg on 8/3/17.
 */
public class VacuumRobot implements MutableObjectInstance {

    public static final String VAR_BATTERY = "batteryLevel";
    public static final String VAR_MAX_BATTERY = "maxBatteryLevel";
    public static final String VAR_HEALTH = "healthLevel";
    public static final String VAR_DOCKED = "docked";
    public static final String VAR_MAX_HEALTH = "maxHealthLevel";
    public static final String VAR_DAMAGED = "damaged";


    public int batteryLevel;

    public int maxBatteryLevel;

    public int healthLevel;
    public int maxHealthLevel;
    public int x;
    public  int y;
    public boolean docked;
    public boolean damaged;

    public boolean canWarn;

    public List<Object> allKeys = Arrays.asList(VAR_BATTERY, VAR_MAX_BATTERY, VAR_HEALTH, VAR_MAX_HEALTH, VAR_X_POS, VAR_Y_POS,
            VAR_DOCKED, VAR_DAMAGED);

    public String name;

    public VacuumRobot() {

    }

    public VacuumRobot(String name, int batteryLevel, int maxBatteryLevel, int healthLevel, int maxHealthLevel, int x, int y, boolean docked, boolean damaged, boolean canWarn) {
        this.name = name;
        this.batteryLevel = batteryLevel;
        this.maxBatteryLevel = maxBatteryLevel;
        this.healthLevel = healthLevel;
        this.maxHealthLevel = maxHealthLevel;
        this.x = x;
        this.y = y;
        this.docked = docked;
        this.damaged = damaged;
        this.canWarn = canWarn;
    }

    @Override
    public String className() {
        return CLASS_ROBOT;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public ObjectInstance copyWithName(String s) {
        return new VacuumRobot(s, this.batteryLevel, this.maxBatteryLevel, this.healthLevel, this.maxHealthLevel,
                this.x, this.y, this.docked, this.damaged, this.canWarn);
    }

    @Override
    public List<Object> variableKeys() {
        return allKeys;
    }

    @Override
    public Object get(Object key) {
        if(key.equals(VAR_BATTERY)) return batteryLevel;
        else if(key.equals(VAR_MAX_BATTERY)) return maxBatteryLevel;
        else if(key.equals(VAR_HEALTH)) return healthLevel;
        else if(key.equals(VAR_MAX_HEALTH)) return maxHealthLevel;
        else if(key.equals(VAR_X_POS)) return x;
        else if(key.equals(VAR_Y_POS)) return y;
        else if(key.equals(VAR_DOCKED)) return docked;
        else if(key.equals(VAR_DAMAGED)) return damaged;
        throw new RuntimeException("[" + this.name + ":get] key " + key + " not found");
    }

    @Override
    public State copy() {
        return new VacuumRobot(this.name, this.batteryLevel, this.maxBatteryLevel, this.healthLevel, this.maxHealthLevel,
                this.x, this.y, this.docked, this.damaged, this.canWarn);
    }

    @Override
    public void set(Object key, Object value) {
        if(key.equals(VAR_BATTERY)) this.batteryLevel = ((Number)value).intValue();
        else if(key.equals(VAR_BATTERY)) this.maxBatteryLevel = ((Number)value).intValue();
        else if(key.equals(VAR_HEALTH)) this.healthLevel = ((Number)value).intValue();
        else if(key.equals(VAR_Y_POS)) this.y = ((Number)value).intValue();
        else if(key.equals(VAR_X_POS)) this.x = ((Number)value).intValue();
        else if(key.equals(VAR_DOCKED)) this.docked = ((Boolean)value).booleanValue();
        else if(key.equals(VAR_DAMAGED)) this.damaged = ((Boolean)value).booleanValue();
        else throw new RuntimeException("[" + this.name + ":set] key " + key + " not found");
    }

    @Override
    public String toString() {
        return OOStateUtilities.objectInstanceToString(this);
    }
}
