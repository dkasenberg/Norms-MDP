/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.single.domains.cleaning;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.NullRewardFunction;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static normativeagents.single.domains.cleaning.CleaningWorldRobot.MAX_BATTERY_LEVEL;

/**
 *
 * @author dkasenberg
 */
public class CleaningWorldDomain implements DomainGenerator {

    public static final String CLASS_PERSON = "person";
    public static final String CLASS_ROOM = "room";
    public static final String CLASS_ROBOT = "robot";
    public static final String ACTION_CLEAN = "vacuum";
    public static final String ACTION_GREET = "greet";
    public static final String ACTION_DOCK = "dock";
    public static final String ACTION_UNDOCK = "undock";
    public static final String ACTION_WAIT = "wait";
    public static final String ACTION_DEAD = "beDead";
    public static final String VAR_DIRT = "dirtAmount";
    public static final String VAR_INROOM = "inRoom";
    public static final String VAR_GREETED = "greeted";
    public static final String VAR_BATTERY = "batteryLevel";
    public static final String VAR_DOCKED = "docked";
    public static final String PF_CLEAN = "roomClean";
    public static final String PF_LOW = "batteryLow";
    public static final String PF_DEAD = "batteryDead";
    public static final String PF_INROOM = "inRoom";
    public static final String PF_GREETED = "greeted";
    public static final String PF_DOCKED = "docked";
    
    protected Random rand;
    
    public CleaningWorldDomain() {
        rand = new Random();
    }
    
    @Override
    public OOSADomain generateDomain() {
        OOSADomain domain = new OOSADomain();
        domain.addStateClass(CLASS_ROBOT, CleaningWorldRobot.class)
                .addStateClass(CLASS_ROOM, CleaningWorldRoom.class)
                .addPropFunction(new DockedPF(PF_DOCKED))
                .addPropFunction(new DeadPF(PF_DEAD))
                .addPropFunction(new CleanPF(PF_CLEAN))
                .addPropFunction(new LowBatteryPF(PF_LOW))
                .addActionType(new CleanActionType(ACTION_CLEAN))
                .addActionType(new WaitActionType(ACTION_WAIT))
                .addActionType(new DockActionType(ACTION_DOCK))
                .addActionType(new UndockActionType(ACTION_UNDOCK))
                .addActionType(new DeadActionType(ACTION_DEAD));

        CleaningWorldModel stateModel = new CleaningWorldModel();
        TerminalFunction tf = new GoalConditionTF(new StateConditionTest() {
            @Override
            public boolean satisfies(State s) {
                OOState st = (OOState)s;
                return ((CleaningWorldRoom)st.object(CLASS_ROOM)).dirt == 0 || ((CleaningWorldRobot)st.object(CLASS_ROBOT)).batteryLevel == 0;
            }
        });

        FactoredModel model = new FactoredModel(stateModel, new NullRewardFunction(), tf);
        domain.setModel(model);

        return domain;
    }

    public static CleaningWorldState initialState(int initRoomDirt, int initRobotBattery) {
        if(initRobotBattery < 0 || initRobotBattery > MAX_BATTERY_LEVEL) {
            throw new RuntimeException("Robot battery must be between 0 and 10");
        } else if(initRoomDirt < 0) {
            throw new RuntimeException("Room dirt cannot be negative");
        }
        return new CleaningWorldState(initRoomDirt,initRobotBattery);
    }
    
    public static class CleanPF extends PropositionalFunction {

        public CleanPF(String name) {
            super(name, new String[0]);
        }
        
        @Override
        public boolean isTrue(OOState s, String[] params) {
            return ((CleaningWorldRoom)s.object(CLASS_ROOM)).dirt == 0;
        }
        
    }
    
    public static class LowBatteryPF extends PropositionalFunction {
        public LowBatteryPF(String name) {
            super(name, new String[0]);
        }

        @Override
        public boolean isTrue(OOState s, String[] params) {
            return ((CleaningWorldRobot)s.object(CLASS_ROBOT)).batteryLevel <= 2;
        }
    }
    
    public static class DeadPF extends PropositionalFunction {
        public DeadPF(String name) {
            super(name, new String[0]);
        }

        @Override
        public boolean isTrue(OOState s, String[] params) {
            return ((CleaningWorldRobot)s.object(CLASS_ROBOT)).batteryLevel == 0;
        }
    }
    
    public static class DockedPF extends PropositionalFunction {
        public DockedPF(String name) {
            super(name, new String[0]);
        }

        @Override
        public boolean isTrue(OOState s, String[] params) {
            return ((CleaningWorldRobot)s.object(CLASS_ROBOT)).docked;
        }
    }

    public class CleanActionType extends UniversalActionType {

        public CleanActionType(String typeName) {
            super(typeName);
        }

        @Override
        public List<Action> allApplicableActions(State s) {
            OOState st = (OOState)s;
            return ((CleaningWorldRobot)st.object(CLASS_ROBOT)).batteryLevel > 0 && !((CleaningWorldRobot)st.object(CLASS_ROBOT)).docked ? allActions : new ArrayList<>();
        }
    }

    public class DockActionType extends UniversalActionType {
        public DockActionType(String typeName) { super(typeName); }

        @Override
        public List<Action> allApplicableActions(State s) {
            OOState st = (OOState)s;
            return ((CleaningWorldRobot)st.object(CLASS_ROBOT)).batteryLevel > 0 && !((CleaningWorldRobot)st.object(CLASS_ROBOT)).docked ? allActions : new ArrayList<>();
        }
    }

    public class UndockActionType extends UniversalActionType {
        public UndockActionType(String typeName) { super(typeName); }

        @Override
        public List<Action> allApplicableActions(State s){
            OOState st = (OOState)s;
            return ((CleaningWorldRobot)st.object(CLASS_ROBOT)).batteryLevel > 0 && ((CleaningWorldRobot)st.object(CLASS_ROBOT)).docked ? allActions : new ArrayList<>();
        }
    }

    public class WaitActionType extends UniversalActionType {
        public WaitActionType(String typeName) { super(typeName); }

        @Override
        public List<Action> allApplicableActions(State s) {
            OOState st = (OOState)s;
            return ((CleaningWorldRobot)st.object(CLASS_ROBOT)).batteryLevel > 0 ? allActions : new ArrayList<>();
        }
    }

    public class DeadActionType extends UniversalActionType {
        public DeadActionType(String typeName) { super(typeName); }

        @Override
        public List<Action> allApplicableActions(State s) {
            OOState st = (OOState)s;
            return ((CleaningWorldRobot)st.object(CLASS_ROBOT)).batteryLevel == 0 ? allActions : new ArrayList<>();
        }
    }
    
}
