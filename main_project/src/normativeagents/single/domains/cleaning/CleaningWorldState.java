package normativeagents.single.domains.cleaning;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;

import java.util.Arrays;
import java.util.List;

import static normativeagents.single.domains.cleaning.CleaningWorldDomain.*;

/**
 * Created by dan on 5/17/17.
 */
public class CleaningWorldState implements MutableOOState {

    public CleaningWorldRoom room;
    public CleaningWorldRobot robot;

    public CleaningWorldState(CleaningWorldRobot robot, CleaningWorldRoom room) {
        this.room = room;
        this.robot = robot;
    }

    public CleaningWorldState(int roomDirt, int batteryLevel) {
        this.room = new CleaningWorldRoom(roomDirt);
        this.robot = new CleaningWorldRobot(batteryLevel);
    }

    public CleaningWorldState() {
        this.room = new CleaningWorldRoom();
        this.robot = new CleaningWorldRobot();
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        throw new RuntimeException("This domain doesn't support adding objects.");
    }

    @Override
    public MutableOOState removeObject(String oname) {
        throw new RuntimeException("This domain doesn't support removing objects.");
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {
        if(objectName.equals(room.name)) {
            room.name = newName;
        } else if(objectName.equals(robot.name)) {
            robot.name = newName;
        }
        return this;
    }

    @Override
    public int numObjects() {
        return 2;
    }

    @Override
    public ObjectInstance object(String oname) {
        if(oname.equals(room.name)) {
            return room;
        } else if(oname.equals(robot.name)) {
            return robot;
        }
        throw new RuntimeException("Object " + oname + " not found.");
    }

    @Override
    public List<ObjectInstance> objects() {
        return Arrays.asList(room, robot);
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(CLASS_ROOM)) {
            return Arrays.asList(room);
        } else if(oclass.equals(CLASS_ROBOT)) {
            return Arrays.asList(robot);
        }
        throw new RuntimeException("Class " + oclass + " not found.");
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);
        if(key.obName.equals(robot.name)) {
            if(key.obVarKey.equals(VAR_BATTERY)) {
                robot.batteryLevel = StateUtilities.stringOrNumber(value).intValue();
                return this;
            } else if(key.obVarKey.equals(VAR_DOCKED)) {
                robot.docked = StateUtilities.stringOrBoolean(value);
                return this;
            }
        } else if(key.obName.equals(room.name) && key.obVarKey.equals(VAR_DIRT)) {
            room.dirt = StateUtilities.stringOrNumber(value).intValue();
            return this;
        }
        throw new RuntimeException("Key " + variableKey + " not found.");
    }

    @Override
    public List<Object> variableKeys() {
        return OOStateUtilities.flatStateKeys(this);
    }

    @Override
    public Object get(Object variableKey) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);
        if(key.obName.equals(robot.name)) {
            return robot.get(key.obVarKey);
        } else if(key.obName.equals(room.name)) {
            return room.get(key.obVarKey);
        }
        throw new RuntimeException("Key " + variableKey + " not found.");
    }

    @Override
    public State copy() {
        return new CleaningWorldState((CleaningWorldRobot)robot.copy(), (CleaningWorldRoom)room.copy());
    }
}
