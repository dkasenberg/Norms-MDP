package normativeagents.single.domains.shocks;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static normativeagents.Helper.powerSet;
import static normativeagents.single.domains.shocks.ShockDomain.*;

/**
 * Created by dan on 5/16/17.
 */
public class ShockWorldModel implements FullStateModel {

    public double probSwitchFailure;
    public double probSpontTransition;

    public ShockWorldModel(double probSwitchFailure, double probSpontTransition) {
        this.probSpontTransition = probSpontTransition;
        this.probSwitchFailure = probSwitchFailure;
    }

    @Override
    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        String aname = a.actionName();
        if(((ShockWorldState)s).victims.values().stream()
                    .anyMatch(v -> v.main
                            || v.alternate)) {
            ShockWorldState failState = (ShockWorldState) s.copy();
            ShockWorldState succState = (ShockWorldState) s.copy();
            failState.victims.values().stream().filter(v -> v.main)
                    .forEach(v -> failState.set(new OOVariableKey(v.name(), VAR_PAIN), true));
            succState.victims.values().stream().filter(v -> v.alternate)
                    .forEach(v -> succState.set(new OOVariableKey(v.name(), VAR_PAIN), true));
            failState.victims.values().forEach(v -> {
                failState.set(new OOVariableKey(v.name(), VAR_MAIN), false);
                failState.set(new OOVariableKey(v.name(), VAR_ALT), false);
            });
            succState.victims.values().forEach(v -> {
                succState.set(new OOVariableKey(v.name(), VAR_MAIN), false);
                succState.set(new OOVariableKey(v.name(), VAR_ALT), false);
            });
            if (aname.equals(ACTION_FLIP)) {

                return Arrays.asList(new StateTransitionProb(failState, probSwitchFailure),
                        new StateTransitionProb(succState, 1.0 - probSwitchFailure));
            } else if (aname.equals(ACTION_STAY)) {
                return Arrays.asList(new StateTransitionProb(failState, 1.0 - probSpontTransition),
                        new StateTransitionProb(succState, probSpontTransition));
            }
        } else if (aname.equals(ACTION_STAY)) {
            ShockWorldState st = (ShockWorldState)s.copy();
            st.victims.values().forEach(v -> st.set(new OOVariableKey(v.name(), VAR_PAIN), false));
            List<StateTransitionProb> tps = new ArrayList<>();
                Set<List<ObjectInstance>> ps = powerSet(new ArrayList<ObjectInstance>(st.victims.values()));
                for(List<ObjectInstance> objs : ps) {
                    ShockWorldState sp = (ShockWorldState)st.copy();
                    objs.stream().forEach(v -> {
                        sp.set(new OOVariableKey(v.name(), VAR_MAIN), true);
                            });

                    sp.victims.values().stream()
                            .filter(v -> !v.main)
                            .forEach(v -> sp.set(new OOVariableKey(v.name(), VAR_ALT), true));

                    double binom = (double)ps.stream().filter(os -> os.size() == objs.size()).count();
                    tps.add(new StateTransitionProb(sp,
                            1.0/((double)(sp.victims
                                    .size()+1)*binom)));
                }
                return tps;
        }
        throw new RuntimeException("Unknown action " + aname);
    }

    @Override
    public State sample(State s, Action a) {
        return Helper.sampleByEnumeration(this, s, a);
    }
}
