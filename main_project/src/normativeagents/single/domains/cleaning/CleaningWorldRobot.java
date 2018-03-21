package normativeagents.single.domains.cleaning;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;

import java.util.Arrays;
import java.util.List;

import static normativeagents.single.domains.cleaning.CleaningWorldDomain.*;

/**
 * Created by dan on 5/17/17.
 */
public class CleaningWorldRobot implements ObjectInstance {

    public int batteryLevel;
    public boolean docked;
    public static int DEFAULT_BATTERY_LEVEL = 10;
    public static int MAX_BATTERY_LEVEL = 10;

    public String name;

    public List<Object> keys = Arrays.asList(VAR_BATTERY, VAR_DOCKED);

    public CleaningWorldRobot(String name, boolean docked, int batteryLevel) {
        this.batteryLevel = batteryLevel;
        this.docked = docked;
        this.name = name;
    }

    public CleaningWorldRobot(int batteryLevel) {
        this.name = CLASS_ROBOT;
        this.docked = false;
        this.batteryLevel = batteryLevel;
    }

    public CleaningWorldRobot() {
        this.name = CLASS_ROBOT;
        this.batteryLevel = DEFAULT_BATTERY_LEVEL;
        this.docked = false;
    }

    @Override
    public String className() {
        return CLASS_ROBOT;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new CleaningWorldRobot(objectName, docked, batteryLevel);
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if(variableKey.equals(VAR_BATTERY)) {
            return batteryLevel;
        } else if(variableKey.equals(VAR_DOCKED)) {
            return docked;
        }
        throw new RuntimeException("Variable " + variableKey + " not recognized for robot class.");
    }

    @Override
    public State copy() {
        return new CleaningWorldRobot(name, docked, batteryLevel);
    }
}
