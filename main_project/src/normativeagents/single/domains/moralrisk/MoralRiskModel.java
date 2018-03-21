package normativeagents.single.domains.moralrisk;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

import java.util.Arrays;
import java.util.List;

import static normativeagents.single.domains.moralrisk.MoralRiskDomain.ACTION_GAMBLE;
import static normativeagents.single.domains.moralrisk.MoralRiskDomain.ACTION_STAY;

/**
 * Created by dan on 5/17/17.
 */
public class MoralRiskModel implements FullStateModel {

    public double probOfSaving;
    public int defaultDead;

    public MoralRiskModel(double probOfSaving, int defaultDead) {
        this.probOfSaving = probOfSaving;
        this.defaultDead = defaultDead;
    }

    @Override
    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        String aname = a.actionName();
        if ((!((MoralRiskState)s).done) && aname.equals(ACTION_GAMBLE)) {
            return gamble(s);
        }
        return Helper.deterministicTransition(this,s,a);
    }

    public List<StateTransitionProb> gamble(State s) {
        MoralRiskState success = (MoralRiskState)s.copy();
        success.done = true;

        MoralRiskState failure = (MoralRiskState)success.copy();
        failure.victims.values().stream().forEach(victim -> {
            victim.dead = true;
        });

        return Arrays.asList(
                new StateTransitionProb(success, probOfSaving),
                new StateTransitionProb(failure, 1.0 - probOfSaving));
    }

    public State stay(State s) {
        MoralRiskState st = (MoralRiskState)s.copy();
        st.done = true;
        st.victims.values().stream().limit(defaultDead).forEach(victim -> victim.dead = true);

        return st;
    }

    @Override
    public State sample(State s, Action a) {
        MoralRiskState st = (MoralRiskState) s;
        if (st.done) {
            return st.copy();
        }
        String aname = a.actionName();
        if (aname.equals(ACTION_GAMBLE)) {
            return Helper.sampleByEnumeration(this, s, a);
        } else if(aname.equals(ACTION_STAY)) {
            return stay(s);
        }
        throw new RuntimeException("Unknown action " + a);
    }
}
