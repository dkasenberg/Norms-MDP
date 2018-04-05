package normativeagents.single.domains.vacuum.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import normativeagents.single.domains.MutableObjectInstance;

import java.util.*;

/**
 * Created by dkasenberg on 8/3/17.
 */
public class VacuumState implements MutableOOState {

    public VacuumRobot robot;
    public Map<String, VacuumObstacle> obstacles = new HashMap<>();
    public Map<String, VacuumMess> messes = new HashMap<>();
    public Map<String, VacuumHuman> humans = new HashMap<>();
    public Map<String, VacuumDocker> dockers = new HashMap<>();

    public Set<String> disallowedMessTypes = new HashSet<>();

    public static final String CLASS_ROBOT = "robot";
    public static final String CLASS_MESS = "mess";
    public static final String CLASS_HUMAN = "human";
    public static final String CLASS_OBSTACLE = "obstacle";
    public static final String CLASS_DOCKER = "docker";

    public static final String VAR_X_POS = "XPosition";
    public static final String VAR_Y_POS = "YPosition";
    public static final String VAR_GONE_MESS = "disallowedMessTypes";

    public VacuumState() {

    }

    public VacuumState(VacuumRobot robot, Map<String, VacuumObstacle> obstacles, Map<String, VacuumMess> messes,
                       Map<String, VacuumHuman> humans, Map<String, VacuumDocker> dockers) {
        this.robot = robot;
        robot.name = CLASS_ROBOT;
        this.obstacles = obstacles;
        this.messes = messes;
        this.dockers = dockers;
        this.humans = humans;
    }

    public VacuumState(VacuumRobot robot, Map<String, VacuumObstacle> obstacles, Map<String, VacuumMess> messes,
                       Map<String, VacuumHuman> humans, Map<String, VacuumDocker> dockers, Set<String> disallowedMessTypes) {
        this(robot, obstacles, messes, humans, dockers);
        this.disallowedMessTypes = new HashSet<>(disallowedMessTypes);
    }

    public void addMess(VacuumMess obj) {
        String newName = CLASS_MESS + obj.x + "_" + obj.y;
        VacuumMess mess = (VacuumMess)obj.copyWithName(newName);
        if(messAt(mess.x, mess.y)) return;
        messes.put(newName, mess);
        if(mess.type.oneTime) {
            this.disallowedMessTypes.add(mess.type.typeName);
        }
    }

    @Override
    public MutableOOState addObject(ObjectInstance objectInstance) {
        if(objectInstance instanceof VacuumHuman) {
            humans.put(objectInstance.name(), (VacuumHuman)objectInstance);
        } else if(objectInstance instanceof VacuumObstacle) {
            obstacles.put(objectInstance.name(), (VacuumObstacle)objectInstance);
        } else if(objectInstance instanceof VacuumMess) {
            this.addMess((VacuumMess)objectInstance);
        } else if(objectInstance instanceof VacuumDocker) {
            dockers.put(objectInstance.name(), (VacuumDocker)objectInstance);
        } else {
            throw new RuntimeException("Object type not recognized for addition to VacuumState");
        }
        return this;
    }

    @Override
    public MutableOOState removeObject(String s) {
        if(obstacles.containsKey(s)) {
            obstacles.remove(s);
        } else if(humans.containsKey(s)) {
            humans.remove(s);
        } else if(messes.containsKey(s)) {
            humans.values().forEach(human->human.knownMesses.remove(s));
            messes.remove(s);
        } else if(dockers.containsKey(s)) {
            dockers.remove(s);
        } else if(s.equals(robot.name())) {
            throw new RuntimeException("Can't remove robot from VacuumState");
        }
        return this;
    }

    @Override
    public String toString() {
        return OOStateUtilities.ooStateToString(this) + "\n" + disallowedMessTypes.toString();
    }

    @Override
    public MutableOOState renameObject(String s, String s1) {
        if(obstacles.containsKey(s)) {
            ObjectInstance objectInstance = obstacles.get(s).copyWithName(s1);
            obstacles.remove(s);
            obstacles.put(s1, (VacuumObstacle)objectInstance);
        } else if(humans.containsKey(s)) {
            ObjectInstance objectInstance = humans.get(s).copyWithName(s1);
            humans.remove(s);
            humans.put(s1, (VacuumHuman)objectInstance);
        } else if(messes.containsKey(s)) {
            ObjectInstance objectInstance = messes.get(s).copyWithName(s1);
            messes.remove(s);
            messes.put(s1, (VacuumMess) objectInstance);
        } else if(dockers.containsKey(s)) {
            ObjectInstance objectInstance = dockers.get(s).copyWithName(s1);
            dockers.remove(s);
            dockers.put(s1, (VacuumDocker) objectInstance);
        } else if(s.equals(robot.name())) {
            throw new RuntimeException("Can't remove robot from VacuumState");
        }
        return this;
    }

    @Override
    public int numObjects() {
        return 1 + humans.size() + messes.size() + obstacles.size() + dockers.size();
    }

    @Override
    public ObjectInstance object(String s) {
        if(obstacles.containsKey(s)) {
            return obstacles.get(s);
        } else if(humans.containsKey(s)) {
            return humans.get(s);
        } else if(messes.containsKey(s)) {
            return messes.get(s);
        } else if(s.equals(robot.name())) {
            return robot;
        } else if(dockers.containsKey(s)) {
            return dockers.get(s);
        }
        return null;
    }

    public void disallowMessType(String messType) {
        this.disallowedMessTypes.add(messType);
    }

    @Override
    public List<ObjectInstance> objects() {
        List<ObjectInstance> allObjects = new ArrayList<>();
        allObjects.add(robot);
        allObjects.addAll(humans.values());
        allObjects.addAll(obstacles.values());
        allObjects.addAll(messes.values());
        allObjects.addAll(dockers.values());
        return allObjects;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String s) {
        if(s.equals(CLASS_ROBOT)) {
            return Collections.singletonList(robot);
        } else if(s.equals(CLASS_HUMAN)) {
            return new ArrayList<>(humans.values());
        } else if(s.equals(CLASS_MESS)) {
            return new ArrayList<>(messes.values());
        } else if(s.equals(CLASS_OBSTACLE)) {
            return new ArrayList<>(obstacles.values());
        } else if(s.equals(CLASS_DOCKER)) {
            return new ArrayList<>(dockers.values());
        }
        throw new RuntimeException("No objects found of class " + s);
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);

        MutableObjectInstance objectToSet;
        if(key.obName.equals(robot.name())) {
            objectToSet = robot;
        } else if(obstacles.containsKey(key.obName)) {
            objectToSet = obstacles.get(key.obName);
        } else if(messes.containsKey(key.obName)) {
            objectToSet = messes.get(key.obName);
        } else if(humans.containsKey(key.obName)) {
            objectToSet = humans.get(key.obName);
        } else if(dockers.containsKey(key.obName)) {
            objectToSet = dockers.get(key.obName);
        } else {
            throw new RuntimeException("Set: Object " + key.obName + " not found");
        }
        objectToSet.set(key.obVarKey, value);
        return this;
    }

    protected VacuumRobot touchRobot() {
        return (VacuumRobot)robot.copy();
    }

    protected Map<String, VacuumHuman> touchHumans() {
        Map<String, VacuumHuman> newHumans = new HashMap<>();
        for(VacuumHuman human : humans.values()) {
            newHumans.put(human.name(), (VacuumHuman)human.copy());
        }
        return newHumans;
    }

    protected Map<String, VacuumObstacle> touchObstacles() {
        Map<String, VacuumObstacle> newObstacles = new HashMap<>();
        for(VacuumObstacle obstacle : obstacles.values()) {
            newObstacles.put(obstacle.name(), (VacuumObstacle)obstacle.copy());
        }
        return newObstacles;
    }

    protected Map<String, VacuumMess> touchMesses() {
        Map<String, VacuumMess> newMesses = new HashMap<>();
        for(VacuumMess mess : messes.values()) {
            newMesses.put(mess.name(), (VacuumMess)mess.copy());
        }
        return newMesses;
    }

    protected Map<String, VacuumDocker> touchDockers() {
        Map<String, VacuumDocker> newDockers = new HashMap<>();
        for(VacuumDocker docker : dockers.values()) {
            newDockers.put(docker.name(), (VacuumDocker)docker.copy());
        }
        return newDockers;
    }

    @Override
    public List<Object> variableKeys() {
        List<Object> toReturn = OOStateUtilities.flatStateKeys(this);
        toReturn.add(VAR_GONE_MESS);
        return toReturn;
    }

    @Override
    public Object get(Object variableKey) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);
        MutableObjectInstance objectToGet;
        if(key.obName.equals(VAR_GONE_MESS)) {
            return this.disallowedMessTypes;
        }
        if(key.obName.equals(robot.name())) {
            objectToGet = robot;
        } else if(obstacles.containsKey(key.obName)) {
            objectToGet = obstacles.get(key.obName);
        } else if(messes.containsKey(key.obName)) {
            objectToGet = messes.get(key.obName);
        } else if(humans.containsKey(key.obName)) {
            objectToGet = humans.get(key.obName);
        } else if(dockers.containsKey(key.obName)) {
            objectToGet = dockers.get(key.obName);
        } else {
            throw new RuntimeException("Set: Object " + key.obName + " not found");
        }
        return objectToGet.get(key.obVarKey);
    }

    @Override
    public State copy() {
        return new VacuumState(touchRobot(), touchObstacles(), touchMesses(), touchHumans(), touchDockers(), this.disallowedMessTypes);
    }

    public boolean messAt(int x, int y) {
        return messes.values().stream().anyMatch(m -> m.x == x && m.y == y);
    }

    public VacuumMess getMessAt(int x, int y) {
        return messes.values().stream().filter(m->m.x == x && m.y == y).findFirst().orElse(null);
    }
}
