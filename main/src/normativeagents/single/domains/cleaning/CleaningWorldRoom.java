package normativeagents.single.domains.cleaning;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;

import java.util.Arrays;
import java.util.List;

import static normativeagents.single.domains.cleaning.CleaningWorldDomain.CLASS_ROOM;
import static normativeagents.single.domains.cleaning.CleaningWorldDomain.VAR_DIRT;

/**
 * Created by dan on 5/17/17.
 */
public class CleaningWorldRoom implements ObjectInstance {

    public int dirt;
    public int DEFAULT_DIRT_LEVEL = 5;

    public String name;
    public List<Object> keys = Arrays.asList(VAR_DIRT);

    public CleaningWorldRoom() {
        this.dirt = DEFAULT_DIRT_LEVEL;
        this.name = CLASS_ROOM;
    }

    public CleaningWorldRoom(String name, int dirt) {
        this.dirt = dirt;
        this.name = name;
    }

    public CleaningWorldRoom(int dirt) {
        this.dirt = dirt;
        this.name = CLASS_ROOM;
    }

    @Override
    public String className() {
        return CLASS_ROOM;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new CleaningWorldRoom(objectName, dirt);
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if(variableKey.equals(VAR_DIRT)) {
            return dirt;
        }
        throw new RuntimeException("Variable " + variableKey + " not recognized for Room object.");
    }

    @Override
    public State copy() {
        return new CleaningWorldRoom(name, dirt);
    }
}
