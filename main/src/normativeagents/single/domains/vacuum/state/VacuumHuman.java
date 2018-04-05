package normativeagents.single.domains.vacuum.state;

import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import normativeagents.single.domains.MutableObjectInstance;

import java.util.*;

import static normativeagents.single.domains.vacuum.state.VacuumState.*;

/**
 * Created by dkasenberg on 8/3/17.
 */
public class VacuumHuman implements MutableObjectInstance {

    public String name;

//    These values should remain constant over this object's life.
    public double movementProbability;
    public double messProbability;

    public double onPhoneMovementProbability;
    public double onPhoneMessProbability;

    public double stayOnPhoneProbability;
    public double startPhoneCallProbability;

    public Set<String> knownMesses;

//    These values will change over this object's life.
//    public int healthLevel;
    public int x;
    public int y;
    public boolean injured;

    public boolean onPhone;
    public boolean justAddressedByRobot;


//    public static final String VAR_HEALTH = "healthLevel";
//    public static final String VAR_MOVEMENT = "movementProb";
//    public static final String VAR_MESS = "messProb";
    public static final String VAR_INJURED = "injured";
    public static final String VAR_ON_PHONE = "onPhone";
    public static final String VAR_KNOWN_MESSES = "knownMesses";
    public static final String VAR_ROBOT_SPOKE = "addressedByRobot";

    protected List<Object> keys = Arrays.asList(
//            VAR_MOVEMENT
//            , VAR_MESS
            VAR_KNOWN_MESSES
            , VAR_X_POS
            , VAR_Y_POS
            , VAR_INJURED
            , VAR_ON_PHONE
            , VAR_ROBOT_SPOKE
//            , VAR_HEALTH
    );

    public VacuumHuman() {

    }

    public VacuumHuman(String name, /*int healthLevel,*/ double movementProbability, double messProbability, int x, int y,
                       boolean injured, double onPhoneMessProbability, double onPhoneMovementProbability,
                       double stayOnPhoneProbability, double startPhoneCallProbability,
                       boolean onPhone, Set<String> knownMesses, boolean justAddressedByRobot) {
        this.name = name;
//        this.healthLevel = healthLevel;
        this.movementProbability =movementProbability;
        this.messProbability = messProbability;
        this.x = x;
        this.y = y;
        this.injured = injured;
        this.onPhone = onPhone;
        this.onPhoneMessProbability = onPhoneMessProbability;
        this.onPhoneMovementProbability = onPhoneMovementProbability;
        this.stayOnPhoneProbability = stayOnPhoneProbability;
        this.startPhoneCallProbability = startPhoneCallProbability;
        this.knownMesses = new HashSet<>(knownMesses);
        this.justAddressedByRobot = justAddressedByRobot;
    }

    public VacuumHuman(String name, /*int healthLevel,*/ double movementProbability, double messProbability, int x, int y) {
        this(name, movementProbability, messProbability, x, y, false, messProbability, movementProbability,
                0.75,0.15,false,Collections.EMPTY_SET,false);
    }

    @Override
    public String className()
    {
        return CLASS_HUMAN;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public ObjectInstance copyWithName(String s) {
        return new VacuumHuman(s,
//                this.healthLevel,
                this.movementProbability,
                this.messProbability,
                this.x,
                this.y,
                this.injured,
                this.onPhoneMessProbability,
                this.onPhoneMovementProbability,
                this.stayOnPhoneProbability,
                this.startPhoneCallProbability,
                this.onPhone,
                this.knownMesses,
                this.justAddressedByRobot);
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object key) {
//        else if(key.equals(VAR_HEALTH)) return this.healthLevel;
        if(key.equals(VAR_X_POS)) return this.x;
        else if(key.equals(VAR_Y_POS)) return this.y;
        else if(key.equals(VAR_INJURED)) return this.injured;
        else if(key.equals(VAR_ON_PHONE)) return this.onPhone;
        else if(key.equals(VAR_KNOWN_MESSES)) return this.knownMesses;
        else if(key.equals(VAR_ROBOT_SPOKE)) return this.justAddressedByRobot;

        throw new RuntimeException("[" + this.name + ":get] key " + key + " not found");
    }

    @Override
    public State copy() {
        return new VacuumHuman(this.name,
//                this.healthLevel,
                this.movementProbability,
                this.messProbability,
                this.x,
                this.y,
                this.injured,
                this.onPhoneMessProbability,
                this.onPhoneMovementProbability,
                this.stayOnPhoneProbability,
                this.startPhoneCallProbability,
                this.onPhone,
                this.knownMesses,
                this.justAddressedByRobot);
    }

    @Override
    public void set(Object key, Object value) {
//        if(key.equals(VAR_MOVEMENT)) this.movementProbability = numVal.doubleValue();
//        else if(key.equals(VAR_HEALTH)) this.healthLevel = numVal.intValue();
//        else if(key.equals(VAR_MESS)) this.messProbability = numVal.doubleValue();
        if(key.equals(VAR_X_POS)) this.x = ((Number)value).intValue();
        else if(key.equals(VAR_Y_POS)) this.y = ((Number)value).intValue();
        else if(key.equals(VAR_INJURED)) this.injured = ((Boolean)value).booleanValue();
        else if(key.equals(VAR_KNOWN_MESSES)) this.knownMesses = (Set<String>)value;
        else if(key.equals(VAR_ROBOT_SPOKE)) this.justAddressedByRobot = ((Boolean)value).booleanValue();
        else if(key.equals(VAR_ON_PHONE)) this.onPhone = ((Boolean)value).booleanValue();
        else {
            throw new RuntimeException("[" + this.name + ":set] key " + key + " not found");
        }
    }

    @Override
    public String toString() {
        return OOStateUtilities.objectInstanceToString(this);
    }
}
