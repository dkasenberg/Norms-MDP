package normativeagents.single.domains.vacuum.state;

import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import normativeagents.single.domains.MutableObjectInstance;
import normativeagents.single.domains.vacuum.mess.VacuumMessType;

import java.util.Arrays;
import java.util.List;

import static normativeagents.single.domains.vacuum.state.VacuumState.*;

/**
 * Created by dkasenberg on 8/3/17.
 */
public class VacuumMess implements MutableObjectInstance {

    public String name;

    public int currentDirtiness;
    public VacuumMessType type;
    public int x;
    public int y;

//    public static final String VAR_ORIG_DIRT = "originalDirtiness";
    public static final String VAR_CURR_DIRT = "currentDirtiness";
//    public static final String VAR_ROBOT_STEP_HAZARD = "robotStepOnHazardLevel";
//    public static final String VAR_HUMAN_HAZARD = "humanHazardLevel";
//    public static final String VAR_ROBOT_VACUUM_HAZARD = "robotVacuumHazardLevel";
    public static final String VAR_MESS_TYPE = "messType";

    protected List<Object> allKeys = Arrays.asList( VAR_CURR_DIRT,
//                                                    VAR_ORIG_DIRT,
//                                                    VAR_ROBOT_STEP_HAZARD,
//                                                    VAR_ROBOT_VACUUM_HAZARD,
//                                                    VAR_HUMAN_HAZARD,
                                                    VAR_X_POS,
                                                    VAR_Y_POS,
                                                    VAR_MESS_TYPE);

    public VacuumMess() {

    }

    public VacuumMess(String name,
                      VacuumMessType type,
                      int currentDirtiness,
                      int x,
                      int y) {
        this.name = name;
        this.currentDirtiness = currentDirtiness;
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public VacuumMess(String name, VacuumMessType type, int x, int y) {
        this(name,type,type.originalDirtiness,x,y);
    }

    @Override
    public String className() {
        return CLASS_MESS;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public ObjectInstance copyWithName(String s) {
        return new VacuumMess(s,
//                this.originalDirtiness,
                this.type,
                this.currentDirtiness,
//                this.robotStepOnHazardLevel,
//                this.robotVacuumHazardLevel,
//                this.humanHazardLevel,
                this.x,
                this.y);
//                this.messType,
//                this.tickFunction).withGraphicsProperties(this.graphicsProperties);
    }

    @Override
    public List<Object> variableKeys() {
        return allKeys;
    }

    @Override
    public Object get(Object key) {
        if(key.equals(VAR_X_POS)) {
            return this.x;
        } else if(key.equals(VAR_Y_POS)) {
            return this.y;
//        } else if(key.equals(VAR_ORIG_DIRT)) {
//            return this.originalDirtiness;
        } else if(key.equals(VAR_CURR_DIRT)) {
            return this.currentDirtiness;
//        } else if(key.equals(VAR_ROBOT_STEP_HAZARD)) {
//            return this.robotStepOnHazardLevel;
//        } else if(key.equals(VAR_ROBOT_VACUUM_HAZARD)) {
//                return this.robotVacuumHazardLevel;
//        } else if(key.equals(VAR_HUMAN_HAZARD)) {
//            return this.humanHazardLevel;
        } else if(key.equals(VAR_MESS_TYPE)) {
            return type.typeName;
        }
        throw new RuntimeException("[" + this.name + ":get] key " + key + " not found");
    }

    @Override
    public State copy() {
        return new VacuumMess(this.name,
//                this.originalDirtiness,
                this.type,
                this.currentDirtiness,
//                this.robotStepOnHazardLevel,
//                this.robotVacuumHazardLevel,
//                this.humanHazardLevel,
                this.x,
                this.y);
//                this.messType,
//                this.tickFunction).withGraphicsProperties(this.graphicsProperties);
    }

    @Override
    public void set(Object key, Object value) {
        Number numVal = StateUtilities.stringOrNumber(value);
        if(key.equals(VAR_X_POS)) {
            this.x = numVal.intValue();
        } else if(key.equals(VAR_Y_POS)) {
            this.y = numVal.intValue();
//        } else if(key.equals(VAR_ORIG_DIRT)) {
//            this.originalDirtiness = numVal.intValue();
        } else if(key.equals(VAR_CURR_DIRT)) {
            this.currentDirtiness = numVal.intValue();
//        } else if(key.equals(VAR_ROBOT_STEP_HAZARD)) {
//            this.robotStepOnHazardLevel = numVal.intValue();
//        } else if(key.equals(VAR_ROBOT_VACUUM_HAZARD)) {
//            this.robotVacuumHazardLevel = numVal.intValue();
//        } else if(key.equals(VAR_HUMAN_HAZARD)) {
//            this.humanHazardLevel = numVal.intValue();
        }
        throw new RuntimeException("[" + this.name + ":get] key " + key + " not found");
    }

//    Sometimes things will happen to messes each turn.  Take care that this function doesn't do something infinite.
    public void tick(VacuumState s) {
//        this.type.tickFunction.accept(s,this);
    }

    @Override
    public String toString() {
        return OOStateUtilities.objectInstanceToString(this);
    }
}
