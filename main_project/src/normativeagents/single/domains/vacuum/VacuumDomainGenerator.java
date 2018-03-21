package normativeagents.single.domains.vacuum;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import normativeagents.single.domains.vacuum.messdistribution.NullMessDistribution;
import normativeagents.single.domains.vacuum.messdistribution.VacuumMessDistribution;
import normativeagents.single.domains.vacuum.state.VacuumHuman;
import normativeagents.single.domains.vacuum.state.VacuumMess;
import normativeagents.single.domains.vacuum.state.VacuumObstacle;
import normativeagents.single.domains.vacuum.state.VacuumRobot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static normativeagents.single.domains.vacuum.state.VacuumHuman.*;
import static normativeagents.single.domains.vacuum.state.VacuumRobot.*;
import static normativeagents.single.domains.vacuum.state.VacuumState.*;

/**
 * Created by dkasenberg on 8/3/17.
 */
public class VacuumDomainGenerator implements DomainGenerator {
    protected int width;
    protected int height;
    protected int numLocationTypes = 1;
    protected int[][] map;
    protected double[][] transitionDynamics;
    protected RewardFunction rf;
    protected TerminalFunction tf;
    protected VacuumMessDistribution messDistribution;
    protected boolean avoidCollisions;

//    TODO generate the propositional functions

    public static final String ACTION_WAIT = "wait";
    public static final String ACTION_VACUUM = "vacuum";
    public static final String ACTION_BEDEAD = "beDead";
    public static final String ACTION_DOCK = "dock";
    public static final String ACTION_UNDOCK = "undock";
    public static final String ACTION_WARN = "warn";

//    public static final String PF_DOCKED = "docked";
//    public static final String PF_ROBOTDEAD = "robotDead";
    public static final String PF_CLEAN = "roomClean";
    public static final String PF_ROBOT_DAMAGED = "robotDamaged";
    public static final String PF_HUMAN_INJURED = "injured";
    public static final String PF_ROBOT_SPOKE = "speak";
    public static final String PF_HUMAN_SPEAKING = "talking";


    public VacuumDomainGenerator(int width, int height) {
        this.width = width;
        this.height = height;
        this.setDeterministicTransitionDynamics();
        this.makeEmptyMap();
        this.messDistribution = new NullMessDistribution();
        this.avoidCollisions = true;
    }

    public VacuumDomainGenerator(int[][] map) {
        this.setMap(map);
        this.setDeterministicTransitionDynamics();
    }

    public void setAvoidCollisions(boolean avoid) {
        this.avoidCollisions = avoid;
    }

    public void setMessDistribution(VacuumMessDistribution distribution) {
        this.messDistribution = distribution;
    }

    public void setDeterministicTransitionDynamics() {
        int na = 4;
        this.transitionDynamics = new double[na][na];

        for(int i = 0; i < na; ++i) {
            for(int j = 0; j < na; ++j) {
                if(i != j) {
                    this.transitionDynamics[i][j] = 0.0D;
                } else {
                    this.transitionDynamics[i][j] = 1.0D;
                }
            }
        }

    }

    public void setProbSucceedTransitionDynamics(double probSucceed) {
        int na = 4;
        double pAlt = (1.0D - probSucceed) / 3.0D;
        this.transitionDynamics = new double[na][na];

        for(int i = 0; i < na; ++i) {
            for(int j = 0; j < na; ++j) {
                if(i != j) {
                    this.transitionDynamics[i][j] = pAlt;
                } else {
                    this.transitionDynamics[i][j] = probSucceed;
                }
            }
        }

    }

    public void setTransitionDynamics(double[][] transitionDynamics) {
        this.transitionDynamics = transitionDynamics.clone();
    }

    public double[][] getTransitionDynamics() {
        double[][] copy = new double[this.transitionDynamics.length][this.transitionDynamics[0].length];

        for(int i = 0; i < this.transitionDynamics.length; ++i) {
            for(int j = 0; j < this.transitionDynamics[0].length; ++j) {
                copy[i][j] = this.transitionDynamics[i][j];
            }
        }

        return copy;
    }

    public void makeEmptyMap() {
        this.map = new int[this.width][this.height];

        for(int i = 0; i < this.width; ++i) {
            for(int j = 0; j < this.height; ++j) {
                this.map[i][j] = 0;
            }
        }

    }

    public void setMap(int[][] map) {
        this.width = map.length;
        this.height = map[0].length;
        this.map = map.clone();
    }

    public void setMapToFourRooms() {
        this.width = 11;
        this.height = 11;
        this.makeEmptyMap();
        this.horizontalWall(0, 0, 5);
        this.horizontalWall(2, 4, 5);
        this.horizontalWall(6, 7, 4);
        this.horizontalWall(9, 10, 4);
        this.verticalWall(0, 0, 5);
        this.verticalWall(2, 7, 5);
        this.verticalWall(9, 10, 5);
    }

    public void horizontalWall(int xi, int xf, int y) {
        for(int x = xi; x <= xf; ++x) {
            this.map[x][y] = 1;
        }

    }

    public void verticalWall(int yi, int yf, int x) {
        for(int y = yi; y <= yf; ++y) {
            this.map[x][y] = 1;
        }

    }

    public void horizontal1DNorthWall(int xi, int xf, int y) {
        for(int x = xi; x <= xf; ++x) {
            int cur = this.map[x][y];
            if(cur != 3 && cur != 4) {
                this.map[x][y] = 2;
            } else {
                this.map[x][y] = 4;
            }
        }

    }

    public void vertical1DEastWall(int yi, int yf, int x) {
        for(int y = yi; y <= yf; ++y) {
            int cur = this.map[x][y];
            if(cur != 2 && cur != 4) {
                this.map[x][y] = 3;
            } else {
                this.map[x][y] = 4;
            }
        }

    }

    public void setObstacleInCell(int x, int y) {
        this.map[x][y] = 1;
    }

    public void set1DNorthWall(int x, int y) {
        int cur = this.map[x][y];
        if(cur != 3 && cur != 4) {
            this.map[x][y] = 2;
        } else {
            this.map[x][y] = 4;
        }

    }

    public void set1DEastWall(int x, int y) {
        int cur = this.map[x][y];
        if(cur != 2 && cur != 4) {
            this.map[x][y] = 3;
        } else {
            this.map[x][y] = 4;
        }

    }

    public void clearLocationOfWalls(int x, int y) {
        this.map[x][y] = 0;
    }

    public void setCellWallState(int x, int y, int wallType) {
        this.map[x][y] = wallType;
    }

    public int[][] getMap() {
        int[][] cmap = new int[this.map.length][this.map[0].length];

        for(int i = 0; i < this.map.length; ++i) {
            for(int j = 0; j < this.map[0].length; ++j) {
                cmap[i][j] = this.map[i][j];
            }
        }

        return cmap;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public RewardFunction getRf() {
        return this.rf;
    }

    public void setRf(RewardFunction rf) {
        this.rf = rf;
    }

    public TerminalFunction getTf() {
        return this.tf;
    }

    public void setTf(TerminalFunction tf) {
        this.tf = tf;
    }

    public List<PropositionalFunction> generatePfs() {
        List<PropositionalFunction> pfs = Arrays.asList(
                new RoomCleanPF(PF_CLEAN)
                , new RobotDamagedPF(PF_ROBOT_DAMAGED)
                , new HumanInjuredPF(PF_HUMAN_INJURED)
                , new RobotSpokeToPersonPF(PF_ROBOT_SPOKE)
                , new HumanSpeakingPF(PF_HUMAN_SPEAKING)
        );
        return pfs;
    }

    public OOSADomain generateDomain() {
        OOSADomain domain = new OOSADomain();
        int[][] cmap = this.getMap();
        domain.addStateClass(CLASS_ROBOT, VacuumRobot.class).addStateClass(CLASS_HUMAN, VacuumHuman.class)
                .addStateClass(CLASS_OBSTACLE, VacuumObstacle.class).addStateClass(CLASS_MESS, VacuumMess.class);
        VacuumModel smodel = new VacuumModel(cmap, this.getTransitionDynamics(), this.messDistribution,this.avoidCollisions);
        RewardFunction rf = this.rf;
        TerminalFunction tf = this.tf;
        if(rf == null) {
            rf = new UniformCostRF();
        }

        if(tf == null) {
            tf = new NullTermination();
        }

        FactoredModel model = new FactoredModel(smodel, rf, tf);
        domain.setModel(model);
        domain.addActionTypes(
                new NotDockedActionType("north"),
                new NotDockedActionType("south"),
                new NotDockedActionType("east"),
                new NotDockedActionType("west"),
                new NotDeadActionType(ACTION_WAIT),
                new NotDockedActionType(ACTION_VACUUM),
                new DockActionType(),
                new UndockActionType(),
                new DeadActionType(ACTION_BEDEAD),
                new WarnActionType(ACTION_WARN)
        );

//        add other action types: vacuum, wait, beDead (vacuum while moving?)

        OODomain.Helper.addPfsToDomain(domain, this.generatePfs());
        return domain;
    }

    protected static int[] movementDirectionFromIndex(int i) {
        int[] result = null;
        switch(i) {
            case 0:
                result = new int[]{0, 1};
                break;
            case 1:
                result = new int[]{0, -1};
                break;
            case 2:
                result = new int[]{1, 0};
                break;
            case 3:
                result = new int[]{-1, 0};
        }

        return result;
    }

    /* ACTION TYPES FOR THIS TEST DOMAIN */

    public static class NotDeadActionType extends UniversalActionType {
        public NotDeadActionType(String typeName) {
            super(typeName);
        }

        @Override
        public List<Action> allApplicableActions(State s) {
            return isRobotDead(s) ?
                    Collections.emptyList() : this.allActions;
        }
    }


    public static class DeadActionType extends UniversalActionType {
        public DeadActionType(String typeName) {
            super(typeName);
        }

        @Override
        public List<Action> allApplicableActions(State s) {
            return isRobotDead(s) ?
                    this.allActions : Collections.EMPTY_LIST;
        }
    }

    public static boolean isRobotDead(State s) {
        return StateUtilities.stringOrNumber(s.get(new OOVariableKey(CLASS_ROBOT,VAR_BATTERY))).intValue() <= 0 ||
                StateUtilities.stringOrNumber(s.get(new OOVariableKey(CLASS_ROBOT, VAR_HEALTH))).intValue() <= 0;
    }

    public static class DockActionType extends UniversalActionType {
        public DockActionType() { super(ACTION_DOCK); }

        @Override
        public List<Action> allApplicableActions(State s) {
            if(!(s instanceof OOState)) return Collections.EMPTY_LIST;
            if(isRobotDead(s)) return Collections.EMPTY_LIST;
            if(StateUtilities.stringOrBoolean(s.get(new OOVariableKey(CLASS_ROBOT, VacuumRobot.VAR_DOCKED))))
                return Collections.EMPTY_LIST;
            return ((OOState)s).objectsOfClass(CLASS_DOCKER).stream().anyMatch(docker ->
                    docker.get(VAR_X_POS).equals(s.get(new OOVariableKey(CLASS_ROBOT, VAR_X_POS))) &&
                    docker.get(VAR_Y_POS).equals(s.get(new OOVariableKey(CLASS_ROBOT, VAR_Y_POS)))) ? this.allActions :
                    Collections.EMPTY_LIST;

        }
    }

    public static class NotDockedActionType extends UniversalActionType {
        public NotDockedActionType(String name) { super(name); }

        @Override
        public List<Action> allApplicableActions(State s) {
            if(isRobotDead(s)) return Collections.EMPTY_LIST;
            return StateUtilities.stringOrBoolean(s.get(new OOVariableKey(CLASS_ROBOT, VacuumRobot.VAR_DOCKED))) ?
                Collections.EMPTY_LIST : this.allActions;
        }
    }


    public static class UndockActionType extends UniversalActionType {
        public UndockActionType() { super(ACTION_UNDOCK); }

        @Override
        public List<Action> allApplicableActions(State s) {
            if(!isRobotDead(s) && StateUtilities.stringOrBoolean(s.get(new OOVariableKey(CLASS_ROBOT,
                    VacuumRobot.VAR_DOCKED))))
                return this.allActions;
            return Collections.EMPTY_LIST;

        }
    }

    public static class WarnActionType extends ObjectParameterizedActionType {

        protected WarnActionType(String name) {
            super(name, new String[]{ CLASS_HUMAN, CLASS_MESS });
        }

//        TODO modify to make adjacent with collision avoidance
        @Override
        protected boolean applicableInState(State state, ObjectParameterizedAction objectParameterizedAction) {
            String[] params = objectParameterizedAction.getObjectParameters();
            VacuumRobot robot = (VacuumRobot)((OOState)state).object(CLASS_ROBOT);
            VacuumHuman human = (VacuumHuman)((OOState)state).object(params[0]);
            return !isRobotDead(state) && !robot.docked && robot.canWarn && human.x == robot.x && human.y == robot.y;
        }
    }

    /* PROPOSITIONAL FUNCTIONS FOR THIS TEST DOMAIN */
    public static class RoomCleanPF extends PropositionalFunction {

        public RoomCleanPF(String name) {
            super(name,new String[0]);
        }

        @Override
        public boolean isTrue(OOState ooState, String... strings) {
            return ooState.objectsOfClass(CLASS_MESS).isEmpty();
        }
    }

    public static class RobotDamagedPF extends PropositionalFunction {
        public RobotDamagedPF(String name) { super(name, new String[0]); }

        @Override
        public boolean isTrue(OOState ooState, String... strings) {
            return StateUtilities.stringOrBoolean(ooState.get(new OOVariableKey(CLASS_ROBOT,VAR_DAMAGED)));
        }
    }

    public static class HumanInjuredPF extends PropositionalFunction {
        public HumanInjuredPF(String name) { super(name, new String[]{ CLASS_HUMAN }); }

        @Override
        public boolean isTrue(OOState ooState, String... strings) {
            return StateUtilities.stringOrBoolean(ooState.get(new OOVariableKey(strings[0],VAR_INJURED)));
        }
    }

    public static class HumanSpeakingPF extends PropositionalFunction {
        public HumanSpeakingPF(String name) { super(name, new String[]{ CLASS_HUMAN}); }

        @Override
        public boolean isTrue(OOState ooState, String... strings) {
            return StateUtilities.stringOrBoolean(ooState.get(new OOVariableKey(strings[0], VAR_ON_PHONE)));
        }
    }

    public static class RobotSpokeToPersonPF extends PropositionalFunction {
        public RobotSpokeToPersonPF(String name) { super(name, new String[]{ CLASS_HUMAN }); }

        @Override
        public boolean isTrue(OOState ooState, String... strings) {
            return StateUtilities.stringOrBoolean(ooState.get(new OOVariableKey(strings[0], VAR_ROBOT_SPOKE)));
        }
    }
}
