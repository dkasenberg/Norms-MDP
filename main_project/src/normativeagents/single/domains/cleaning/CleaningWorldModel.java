package normativeagents.single.domains.cleaning;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

import java.util.List;

import static normativeagents.single.domains.cleaning.CleaningWorldDomain.*;
import static normativeagents.single.domains.cleaning.CleaningWorldRobot.MAX_BATTERY_LEVEL;

/**
 * Created by dan on 5/17/17.
 */
public class CleaningWorldModel implements FullStateModel {
    @Override
    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        return Helper.deterministicTransition(this, s, a);
    }

    @Override
    public State sample(State s, Action a) {
        String aname = a.actionName();
        if(aname.equals(ACTION_CLEAN)) {
            return clean(s);
        } else if(aname.equals(ACTION_WAIT)) {
            return wait(s);
        } else if(aname.equals(ACTION_DOCK)) {
            return dock(s);
        } else if(aname.equals(ACTION_UNDOCK)) {
            return undock(s);
        } else if(aname.equals(ACTION_DEAD)) {
            return beDead(s);
        }
        throw new RuntimeException("Action not found: " + aname);
    }

    public State dock(State s) {
        CleaningWorldState st = (CleaningWorldState)s.copy();
        st.robot.docked = true;
        return st;
    }

    public State undock(State s) {
        CleaningWorldState st = (CleaningWorldState)s.copy();
        st.robot.docked = false;
        return st;
    }

    public State wait(State s) {
        CleaningWorldState st = (CleaningWorldState)s.copy();
        if(st.robot.docked) st.robot.batteryLevel = Math.min(st.robot.batteryLevel + 3, MAX_BATTERY_LEVEL);
        else st.robot.batteryLevel = Math.max(st.robot.batteryLevel - 1, 0);
        return st;
    }

    public State clean(State s) {
        CleaningWorldState st = (CleaningWorldState)s.copy();
        st.robot.batteryLevel = Math.max(st.robot.batteryLevel-1,0);
        st.room.dirt = Math.max(st.room.dirt -1, 0);
        return st;
    }

    public State beDead(State s) {
        return s.copy();
    }
}
