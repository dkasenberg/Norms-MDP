package normativeagents.single.domains.slimchance;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

import java.util.Arrays;
import java.util.List;

import static normativeagents.single.domains.slimchance.SlimChanceDomain.ACTION_NOTRY;
import static normativeagents.single.domains.slimchance.SlimChanceDomain.ACTION_TRY;

/**
 * Created by dan on 5/17/17.
 */
public class SlimChanceModel implements FullStateModel {

    public double probOfSuccess;

    public SlimChanceModel(double probOfSuccess) {
        this.probOfSuccess = probOfSuccess;
    }

    @Override
    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        String aname = a.actionName();
        SlimChanceState bad = (SlimChanceState)s.copy();
        bad.good = false;

        if(aname.equals(ACTION_TRY)) {
            SlimChanceState good = (SlimChanceState)s.copy();
            good.good = true;
            return Arrays.asList(new StateTransitionProb(good, probOfSuccess),
                    new StateTransitionProb(bad, 1.0-probOfSuccess));

        } else if(aname.equals(ACTION_NOTRY)) {
            return Arrays.asList(new StateTransitionProb(bad, 1.0));
        }
        throw new RuntimeException("Unrecognized action " + a);
    }

    @Override
    public State sample(State s, Action a) {
        return Helper.sampleByEnumeration(this, s, a);
    }
}
